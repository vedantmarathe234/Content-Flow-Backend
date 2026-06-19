package com.athenura.contentflow.content.service;

import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.content.dto.*;
import com.athenura.contentflow.commons.enums.ContentStatus;
import com.athenura.contentflow.content.entity.Content;
import com.athenura.contentflow.content.entity.Notification;
import com.athenura.contentflow.content.repository.ContentRepository;
import com.athenura.contentflow.content.repository.NotificationRepository;
import com.athenura.contentflow.department.repository.DepartmentRepository;
import com.athenura.contentflow.email.service.EmailNotificationService;
import com.athenura.contentflow.exception.ResourceNotFoundException;
import com.athenura.contentflow.exception.UnauthorizedException;
import com.athenura.contentflow.team.entity.Team;
import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final com.athenura.contentflow.team.repository.TeamRepository teamRepository;
    private final NotificationRepository notificationRepository;
    private final DepartmentRepository departmentRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final EmailNotificationService emailNotificationService;


    @Transactional
    public ContentResponse createContent(CreateContentRequest request, MultipartFile file, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String fileUrl;

        if ("DRIVE".equalsIgnoreCase(request.getUploadProvider())) {
            if (request.getGoogleDriveLink() == null || request.getGoogleDriveLink().isEmpty()) {
                throw new RuntimeException("Google Drive link is required when DRIVE is selected");
            }
            fileUrl = request.getGoogleDriveLink();
        } else {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File is required for Cloudinary upload");
            }
            fileUrl = cloudinaryService.uploadFile(file);
        }

        Content content = new Content();
        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setMediaUrl(fileUrl);
        content.setUploadProvider(request.getUploadProvider().toUpperCase());
        content.setScheduledDate(request.getScheduledDate());
        content.setCreatedBy(user);
        content.setDepartment(user.getDepartment());

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                                    "Team not found with id: " + request.getTeamId()));
            content.setTeam(team);

            if (team.getTeamLeader() != null && team.getTeamLeader().getId().equals(user.getId())) {
                content.setStatus(ContentStatus.PENDING);
            } else {
                content.setStatus(ContentStatus.PENDING_LEADER);
            }
        } else {
            content.setTeam(null);
            content.setStatus(ContentStatus.PENDING);
        }

        Content savedContent = contentRepository.save(content);
        createNotification(savedContent);

        return mapToResponse(savedContent);

    }

    public List<ContentResponse> getAllContent() {
        return contentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    public List<ContentResponse> getContentByTeamId(Long teamId) {
        return contentRepository.findByTeamId(teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ContentResponse> getMyContents(String email) {
        return contentRepository.findByCreatedByEmail(email).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ContentResponse> getContentByStatus(ContentStatus status) {
        return contentRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ContentResponse> getContentByDepartment(Long departmentId) {
        return contentRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public ContentResponse updateAndResubmit(
            Long id,
            CreateContentRequest request,
            MultipartFile file,
            String email
    ) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!content.getCreatedBy().getId()
                .equals(currentUser.getId())) {
            throw new UnauthorizedException("Only content creator can edit content");
        }

        if (content.getStatus().equals(ContentStatus.APPROVED)) {
            throw new RuntimeException("Cannot edit content that is already APPROVED");
        }

        if ("DRIVE".equalsIgnoreCase(request.getUploadProvider())) {
            if (request.getGoogleDriveLink() != null && !request.getGoogleDriveLink().isEmpty()) {
                content.setMediaUrl(request.getGoogleDriveLink());
            }
        } else if (file != null && !file.isEmpty()) {
            try {
                if (content.getPublicId() != null) {
                    cloudinaryService.deleteFile(content.getPublicId());
                }
                Map uploadResult = cloudinaryService.uploadFileWithDetails(file);
                content.setMediaUrl(uploadResult.get("url").toString());
                content.setPublicId(uploadResult.get("public_id").toString());
            } catch (Exception e) {
                throw new RuntimeException("Cloudinary upload failed: " + e.getMessage());
            }
        }

        if (request.getTitle() != null) {
            content.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            content.setDescription(request.getDescription());
        }

        if (content.getTeam() != null) {
            boolean isLeaderResubmitting = content.getTeam().getTeamLeader() != null
                    && content.getTeam().getTeamLeader().getId().equals(currentUser.getId());

            if (isLeaderResubmitting) {
                content.setStatus(ContentStatus.PENDING);
            } else {
                content.setStatus(ContentStatus.PENDING_LEADER);
            }
        } else {
            content.setStatus(ContentStatus.PENDING);
        }
        content.setRejectionReason(null);
        content.setLeaderApprovedBy(null);
        content.setAdminApprovedBy(null);
        content.setActionDate(null);
        content.setReminderSent(false);
        Content savedContent = contentRepository.save(content);
        createResubmissionNotification(savedContent);
        return mapToResponse(savedContent);
    }

    @Transactional
    public ApiResponse deleteContent(Long id, String email) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Content not found"));

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!content.getCreatedBy().getEmail().equals(email)
                && !currentUser.getRole().name().equals("ADMIN")) {

            throw new UnauthorizedException(
                    "You are not authorized to delete this content");
        }

        if (content.getStatus() == ContentStatus.APPROVED) {
            throw new RuntimeException("You cannot delete approved content");
        }

        contentRepository.delete(content);

        return new ApiResponse("Content deleted successfully");
    }

    @Transactional
    public ApiResponse approveContent(Long id, String email) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can approve content");
        }
        content.setStatus(ContentStatus.APPROVED);
        content.setActionDate(LocalDateTime.now());
        content.setAdminApprovedBy(currentUser.getName());
        content.setReminderSent(true);
        contentRepository.save(content);

        emailNotificationService.sendAdminApprovalEmail(content);

        Notification notification = new Notification();
        notification.setUser(content.getCreatedBy());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setMessage(content.getCreatedBy().getName() + "'s content '" + content.getTitle() + "' was approved");
        notification.setContentId(content.getId());
        notificationRepository.save(notification);
        webSocketNotificationService.sendToUser(notification.getUser().getId(), notification);
        return new ApiResponse("Content approved successfully");
    }

    @Transactional
    public ApiResponse rejectContent(Long id, RejectContentRequest request, String email) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can reject content");
        }

        content.setStatus(ContentStatus.REJECTED);
        content.setRejectionReason(request.getReason());
        content.setActionDate(LocalDateTime.now());
        content.setAdminApprovedBy("Rejected by " + currentUser.getName());
        content.setReminderSent(true);
        contentRepository.save(content);

        emailNotificationService.sendAdminRejectionEmail(content, request.getReason());

        Notification notification = new Notification();
        notification.setUser(content.getCreatedBy());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setMessage(content.getCreatedBy()
                .getName() + "'s content '" + content.getTitle() + "' was rejected by Admin " + currentUser.getName());
        notification.setContentId(content.getId());
        notificationRepository.save(notification);
        webSocketNotificationService.sendToUser(notification.getUser().getId(), notification);

        return new ApiResponse("Content rejected successfully");
    }


    @Transactional
    public ApiResponse approveContentByLeader(Long id, String email) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Content not found"));

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (content.getTeam() == null || content.getTeam().getTeamLeader() == null) {
            throw new UnauthorizedException("No team leader assigned");
        }

        if (!content.getTeam().getTeamLeader().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only this team's leader can approve content");
        }

        if (!content.getStatus().equals(ContentStatus.PENDING_LEADER)) {
            throw new RuntimeException("This content is not pending for leader approval");
        }

        content.setStatus(ContentStatus.PENDING);
        content.setActionDate(LocalDateTime.now());
        content.setLeaderApprovedBy(currentUser.getName());
        content.setReminderSent(false);
        contentRepository.save(content);

        String internName = content.getCreatedBy() != null ? content.getCreatedBy().getName() : "An intern";
        String contentTitle = content.getTitle();
        String detailedAdminMessage = String.format("%s's content '%s' is awaiting admin approval", internName, contentTitle);

        List<User> admins = userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setUser(admin);
            notification.setContentId(content.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setMessage(detailedAdminMessage);

            notificationRepository.save(notification);
            webSocketNotificationService.sendToUser(notification.getUser().getId(), notification);
        }

        return new ApiResponse("Content approved by leader and forwarded to Admin successfully");
    }

    @Transactional
    public ApiResponse rejectContentByLeader(
            Long id,
            RejectContentRequest request,
            String email
    ) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (content.getTeam() == null || content.getTeam().getTeamLeader() == null) {
            throw new UnauthorizedException("No team leader assigned");
        }

        if (!content.getTeam().getTeamLeader().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only this team's leader can reject content");
        }

        content.setStatus(ContentStatus.REJECTED);
        content.setRejectionReason(request.getReason());
        content.setActionDate(LocalDateTime.now());
        content.setLeaderApprovedBy("Rejected by " + currentUser.getName());
        content.setReminderSent(true);
        contentRepository.save(content);
        emailNotificationService.sendLeaderRejectionEmail(content, request.getReason());

        Notification notification = new Notification();
        notification.setUser(content.getCreatedBy());
        notification.setContentId(content.getId());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setMessage(content.getCreatedBy()
                .getName() + "'s content '" + content.getTitle() + "' was rejected by Team Leader");
        notificationRepository.save(notification);
        webSocketNotificationService.sendToUser(notification.getUser().getId(), notification);

        return new ApiResponse("Content rejected by leader");
    }

    public ContentResponse getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));
        return mapToResponse(content);
    }

    private ContentResponse mapToResponse(Content content) {
        ContentResponse response = new ContentResponse();
        response.setId(content.getId());
        response.setProfilePhotoUrl(content.getCreatedBy().getProfilePhotoUrl());
        response.setTitle(content.getTitle());
        response.setDescription(content.getDescription());
        response.setMediaUrl(content.getMediaUrl());
        response.setUploadProvider(content.getUploadProvider());
        response.setStatus(content.getStatus());
        response.setRejectionReason(content.getRejectionReason());
        response.setCreatedBy(content.getCreatedBy().getName());
        response.setCreatedById(content.getCreatedBy().getId());
        response.setTeam(content.getTeam() != null ? content.getTeam().getName() : "Individual");
        response.setDepartment(content.getDepartment() != null ? content.getDepartment().getName() : "Individual");

        if (content.getTeam() != null && content.getTeam().getTeamLeader() != null) {
            response.setTeamLeaderId(content.getTeam().getTeamLeader().getId());
        }

        response.setCreatedAt(content.getCreatedAt());
        response.setScheduledDate(content.getScheduledDate());
        response.setActionDate(content.getActionDate());
        response.setLeaderApprovedBy(content.getLeaderApprovedBy());
        response.setAdminApprovedBy(content.getAdminApprovedBy());

        System.out.println("Leader Approved By = " + content.getLeaderApprovedBy());


        if (content.getStatus() == ContentStatus.PENDING_LEADER) {
            response.setCurrentStage("Pending with Team Leader");
            response.setApprovedByLeader("Not approved yet by Leader");
            response.setApprovedByAdmin("Waiting for Leader Approval");
        }

        else if (content.getStatus() == ContentStatus.PENDING) {
            response.setCurrentStage("Pending with Admin");
            response.setApprovedByLeader(content.getLeaderApprovedBy() != null
                            ? "Approved by: " + content.getLeaderApprovedBy() : "");
            response.setApprovedByAdmin("Pending Admin Approval");
        }

        else if (content.getStatus() == ContentStatus.APPROVED) {
            response.setCurrentStage("Approved by Admin");
            response.setApprovedByLeader(content.getLeaderApprovedBy() != null
                            ? "Approved by: " + content.getLeaderApprovedBy() : "");
            response.setApprovedByAdmin(content.getAdminApprovedBy() != null
                            ? "Approved by: " + content.getAdminApprovedBy() : "");
        }
        else if (content.getStatus() == ContentStatus.REJECTED) {
            response.setCurrentStage("Rejected");

            if (content.getAdminApprovedBy() != null) {
                response.setApprovedByLeader(content.getLeaderApprovedBy() != null
                                ? "Approved by: " + content.getLeaderApprovedBy() : "Not approved");
                response.setApprovedByAdmin(content.getAdminApprovedBy());

            } else {
                response.setApprovedByLeader(content.getLeaderApprovedBy());
                response.setApprovedByAdmin("Not sent to Admin for approval.");
            }
        }
        return response;
    }

    private void createNotification(Content content) {
        Notification notification = new Notification();
        notification.setCreatedAt(LocalDateTime.now());

        if (content.getStatus() == ContentStatus.PENDING_LEADER) {
            notification.setUser(content.getTeam().getTeamLeader());
            notification.setContentId(content.getId());
            notification.setMessage(content.getCreatedBy().getName() + " submitted new content: "
                            + content.getTitle());

            notificationRepository.save(notification);
            webSocketNotificationService.sendToUser(
                    notification.getUser().getId(),
                    notification);

        } else {

            List<User> admins = userRepository.findByRole(Role.ADMIN);

            boolean isTeamLeaderUpload = content.getTeam() != null
                    && content.getTeam().getTeamLeader() != null
                    && content.getCreatedBy().getId().equals(content.getTeam().getTeamLeader().getId());


            String message = isTeamLeaderUpload
                    ? content.getCreatedBy().getName() + " (Team Leader) submitted new content: " + content.getTitle()
                    : content.getCreatedBy().getName() + " submitted new content: " + content.getTitle();

            for (User admin : admins) {
                Notification adminNotification = new Notification();
                adminNotification.setUser(admin);
                adminNotification.setCreatedAt(LocalDateTime.now());
                adminNotification.setContentId(content.getId());
                adminNotification.setMessage(message);

                notificationRepository.save(adminNotification);
                webSocketNotificationService.sendToUser(adminNotification.getUser().getId(), adminNotification);
            }
        }
    }

    private void createResubmissionNotification(
            Content content
    ) {
        if (content.getStatus() == ContentStatus.PENDING_LEADER) {

            Notification notification = new Notification();
            notification.setUser(content.getTeam().getTeamLeader());
            notification.setContentId(content.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setMessage(content.getCreatedBy().getName() + " resubmitted content: "
                            + content.getTitle());

            notificationRepository.save(notification);
            webSocketNotificationService.sendToUser(notification.getUser().getId(), notification);

        } else {

            List<User> admins = userRepository.findByRole(Role.ADMIN);

            for (User admin : admins) {
                Notification notification = new Notification();
                notification.setUser(admin);
                notification.setContentId(content.getId());
                notification.setCreatedAt(LocalDateTime.now());
                notification.setMessage(content.getCreatedBy().getName() + " resubmitted content: "
                                + content.getTitle());

                notificationRepository.save(notification);
                webSocketNotificationService.sendToUser(notification.getUser().getId(), notification);
            }
        }
    }

    public DashboardStatsResponse getDashboardStats() {

        DashboardStatsResponse response =
                new DashboardStatsResponse();

        response.setTotalContent(contentRepository.count());
        response.setPendingLeader(contentRepository.countByStatus(ContentStatus.PENDING_LEADER));
        response.setPendingAdmin(contentRepository.countByStatus(ContentStatus.PENDING));
        response.setApproved(contentRepository.countByStatus(ContentStatus.APPROVED));
        response.setRejected(contentRepository.countByStatus(ContentStatus.REJECTED));
        response.setTotalTeams(teamRepository.count());
        response.setTotalDepartments(departmentRepository.count());
        response.setTotalUsers(userRepository.count());

        Map<String, Long> weeklyMap = new LinkedHashMap<>();
        weeklyMap.put("Mon", 0L);
        weeklyMap.put("Tue", 0L);
        weeklyMap.put("Wed", 0L);
        weeklyMap.put("Thu", 0L);
        weeklyMap.put("Fri", 0L);
        weeklyMap.put("Sat", 0L);
        weeklyMap.put("Sun", 0L);

        try {

            java.time.LocalDateTime startOfWeek = java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
            java.time.LocalDateTime endDate = java.time.LocalDate.now().with(java.time.DayOfWeek.SUNDAY).atTime(23, 59, 59);
            List<Object[]> rawData = contentRepository.countContentGroupedByDay(startOfWeek, endDate);
            for (Object[] row : rawData) {
                String fullDayName = String.valueOf(row[0]);
                long count = ((Number) row[1]).longValue();

                String shortKey = fullDayName.substring(0, 3);
                if (weeklyMap.containsKey(shortKey)) {
                    weeklyMap.put(shortKey, count);
                }
            }
        } catch (Exception e) {
            System.err.println("Error aggregating chart values: " + e.getMessage());
        }

        List<DashboardStatsResponse.DayActivityDTO> activityList = new ArrayList<>();
        weeklyMap.forEach((day, count) ->
                activityList.add(new DashboardStatsResponse.DayActivityDTO(day, count))
        );

        response.setWeeklyActivity(activityList);

        List<Object[]> deptRows = departmentRepository.getDepartmentContentCounts();
        List<DashboardStatsResponse.DepartmentCountDTO> topDeptsList = new ArrayList<>();

        for (Object[] row : deptRows) {
            String deptName = String.valueOf(row[0]);
            long count = ((Number) row[1]).longValue();
            topDeptsList.add(new DashboardStatsResponse.DepartmentCountDTO(deptName, count));
        }

        response.setTopDepartments(topDeptsList);

        return response;
    }

    public UserDashboardResponse getMyDashboardStats(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserDashboardResponse response = new UserDashboardResponse();

        long totalContent = contentRepository.countByCreatedByIdAndTeamIsNull(currentUser.getId());
        long leaderPending = contentRepository.countByCreatedByIdAndTeamIsNullAndStatus(currentUser.getId(), ContentStatus.PENDING_LEADER);
        long adminPending = contentRepository.countByCreatedByIdAndTeamIsNullAndStatus(currentUser.getId(), ContentStatus.PENDING);
        long approved = contentRepository.countByCreatedByIdAndTeamIsNullAndStatus(currentUser.getId(), ContentStatus.APPROVED);
        long rejected = contentRepository.countByCreatedByIdAndTeamIsNullAndStatus(currentUser.getId(), ContentStatus.REJECTED);

        response.setTotalContent(totalContent);
        response.setPendingLeader((int) leaderPending);
        response.setPendingAdmin((int) adminPending);
        response.setApproved((int) approved);
        response.setRejected((int) rejected);
        response.setWeeklyActivity(fetchWeeklyActivityData(null, currentUser.getId()));

        return response;
    }

    public DashboardStatsResponse getTeamDashboardStats(Long teamId, String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        DashboardStatsResponse response = new DashboardStatsResponse();

        response.setTotalContent(contentRepository.countByTeamId(teamId));
        response.setPendingLeader(contentRepository.countByTeamIdAndStatus(teamId, ContentStatus.PENDING_LEADER));
        response.setPendingAdmin(contentRepository.countByTeamIdAndStatus(teamId, ContentStatus.PENDING));
        response.setApproved(contentRepository.countByTeamIdAndStatus(teamId, ContentStatus.APPROVED));
        response.setRejected(contentRepository.countByTeamIdAndStatus(teamId, ContentStatus.REJECTED));
        response.setWeeklyActivity(fetchWeeklyActivityData(teamId, null));

        return response;
    }

    private List<DashboardStatsResponse.DayActivityDTO> fetchWeeklyActivityData(Long teamId, Long userId) {
        Map<String, Long> weeklyMap = new LinkedHashMap<>();
        weeklyMap.put("Mon", 0L);
        weeklyMap.put("Tue", 0L);
        weeklyMap.put("Wed", 0L);
        weeklyMap.put("Thu", 0L);
        weeklyMap.put("Fri", 0L);
        weeklyMap.put("Sat", 0L);
        weeklyMap.put("Sun", 0L);

        try {
            java.time.LocalDateTime startOfWeek = java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
            java.time.LocalDateTime currentDate = java.time.LocalDateTime.now();
            List<Object[]> rawData;

            if (teamId != null && userId != null) {
                rawData = contentRepository.countUserTeamContentGroupedByDay(teamId, userId, startOfWeek, currentDate);
            } else if (teamId != null) {
                rawData = contentRepository.countTeamContentGroupedByDay(teamId, startOfWeek, currentDate);
            } else {
                rawData = contentRepository.countIndividualContentGroupedByDay(userId, startOfWeek, currentDate);
            }

            for (Object[] row : rawData) {
                String fullDayName = String.valueOf(row[0]);
                long count = ((Number) row[1]).longValue();
                String shortKey = fullDayName.substring(0, 3);
                if (weeklyMap.containsKey(shortKey)) {
                    weeklyMap.put(shortKey, count);
                }
            }
        } catch (Exception e) {
            System.err.println("Error aggregating chart dashboard values: " + e.getMessage());
        }

        List<DashboardStatsResponse.DayActivityDTO> activityList = new ArrayList<>();
        weeklyMap.forEach((day, count) -> activityList.add(new DashboardStatsResponse.DayActivityDTO(day, count)));
        return activityList;
    }

}