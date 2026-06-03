package com.athenura.contentflow.content.service;

import com.athenura.contentflow.commons.enums.Role;
import com.athenura.contentflow.content.dto.*;
import com.athenura.contentflow.commons.enums.ContentStatus;
import com.athenura.contentflow.content.entity.Content;
import com.athenura.contentflow.content.entity.Notification;
import com.athenura.contentflow.content.repository.ContentRepository;
import com.athenura.contentflow.content.repository.NotificationRepository;
import com.athenura.contentflow.email.dto.EmailRequest;
import com.athenura.contentflow.email.service.EmailService;
import com.athenura.contentflow.exception.ResourceNotFoundException;
import com.athenura.contentflow.exception.UnauthorizedException;
import com.athenura.contentflow.user.entity.User;
import com.athenura.contentflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;
    private final com.athenura.contentflow.team.repository.TeamRepository teamRepository;
    private final NotificationRepository notificationRepository;

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
            com.athenura.contentflow.team.entity.Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + request.getTeamId()));
            content.setTeam(team);
            content.setStatus(ContentStatus.PENDING_LEADER);
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
    public ContentResponse updateAndResubmit(Long id, CreateContentRequest request, MultipartFile file) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));

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

        content.setStatus(ContentStatus.PENDING);
        content.setRejectionReason(null);
        content.setActionDate(null);
        Content savedContent = contentRepository.save(content);
        return mapToResponse(savedContent);
    }

    @Transactional
    public ApiResponse deleteContent(Long id, String email) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!content.getCreatedBy().getEmail().equals(email) && !currentUser.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("You are not authorized to delete this content");
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
        contentRepository.save(content);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(content.getCreatedBy().getEmail())
                .subject("Content Approved Successfully")
                .body(
                        "<h2>Your content has been approved</h2>" +
                                "<p><b>Title:</b> " + content.getTitle() + "</p>" +
                                "<p><b>Department:</b> " + content.getDepartment().getName() + "</p>" +
                                "<p><b>Status:</b> APPROVED</p>"
                )
                .build();

        emailService.sendEmail(emailRequest);

        Notification notification =
                new Notification();

        notification.setUser(
                content.getCreatedBy()
        );

        notification.setCreatedAt(
                LocalDateTime.now()
        );

        notification.setMessage(
                "Your content '"
                        + content.getTitle()
                        + "' was approved"
        );
        notification.setContentId(content.getId());
        notificationRepository.save(notification);

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
        contentRepository.save(content);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(content.getCreatedBy().getEmail())
                .subject("Content Rejected")
                .body(
                        "<h2>Your content was rejected</h2>" +
                                "<p><b>Title:</b> " + content.getTitle() + "</p>" +
                                "<p><b>Status:</b> REJECTED</p>" +
                                "<p><b>Reason:</b> " + request.getReason() + "</p>"
                )
                .build();

        emailService.sendEmail(emailRequest);

        Notification notification =
                new Notification();

        notification.setUser(
                content.getCreatedBy()
        );

        notification.setCreatedAt(
                LocalDateTime.now()
        );

        notification.setMessage(
                "Your content '"
                        + content.getTitle()
                        + "' was rejected"
        );
        notification.setContentId(content.getId());
        notificationRepository.save(notification);

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

        if (!currentUser.isTeamLeader()) {

            throw new UnauthorizedException(
                    "Only Team Leaders can approve content"
            );
        }

        if (content.getTeam() == null
                || content.getTeam().getTeamLeader() == null) {

            throw new RuntimeException(
                    "This content has no assigned team leader"
            );
        }

        if (content.getTeam()
                .getTeamLeader()
                .getId() != currentUser.getId()) {

            throw new UnauthorizedException(
                    "You are not leader of this team"
            );
        }

        if (!content.getStatus()
                .equals(ContentStatus.PENDING_LEADER)) {

            throw new RuntimeException(
                    "This content is not pending for leader approval"
            );
        }

        content.setStatus(ContentStatus.PENDING);

        content.setActionDate(LocalDateTime.now());

        contentRepository.save(content);

        List<User> admins =
                userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {

            Notification notification =
                    new Notification();

            notification.setUser(admin);
            notification.setContentId(content.getId());
            notification.setCreatedAt(
                    LocalDateTime.now()
            );

            notification.setMessage(
                    content.getTitle()
                            + " is awaiting admin approval"
            );

            notificationRepository.save(notification);
        }

        return new ApiResponse(
                "Content approved by leader and forwarded to Admin successfully"
        );
    }

    public ContentResponse getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));
        return mapToResponse(content);
    }

    private ContentResponse mapToResponse(Content content) {
        ContentResponse response = new ContentResponse();
        response.setId(content.getId());
        response.setTitle(content.getTitle());
        response.setDescription(content.getDescription());
        response.setMediaUrl(content.getMediaUrl());
        response.setUploadProvider(content.getUploadProvider());
        response.setStatus(content.getStatus());
        response.setRejectionReason(content.getRejectionReason());

        response.setCreatedBy(content.getCreatedBy().getName());
        response.setTeam(
                content.getTeam() != null
                        ? content.getTeam().getName()
                        : "Individual"
        );
        response.setDepartment(
                content.getDepartment() != null
                        ? content.getDepartment().getName()
                        : "Individual"
        );


        response.setCreatedAt(content.getCreatedAt());
        response.setScheduledDate(content.getScheduledDate());
        response.setActionDate(content.getActionDate());


        if (content.getStatus() == ContentStatus.PENDING_LEADER) {
            response.setCurrentStage("Pending with Team Leader");
            response.setApprovedByLeader("Not approved yet by Leader");
        } else if (content.getStatus() == ContentStatus.PENDING) {
            response.setCurrentStage("Pending with Admin");
            if (content.getTeam() != null && content.getTeam().getTeamLeader() != null) {
                response.setApprovedByLeader("Approved by: " + content.getTeam().getTeamLeader().getName());
            } else {
                response.setApprovedByLeader("");
            }
        } else if (content.getStatus() == ContentStatus.APPROVED) {
            response.setCurrentStage("Final Approved by Admin ");
            if (content.getTeam() != null && content.getTeam().getTeamLeader() != null) {
                response.setApprovedByLeader("Approved by: " + content.getTeam().getTeamLeader().getName());
            } else {
                response.setApprovedByLeader("");
            }
        } else if (content.getStatus() == ContentStatus.REJECTED) {
            response.setCurrentStage("Rejected");
            response.setApprovedByLeader("Rejected - Check reason");
        }

        return response;
    }

    private void createNotification(Content content) {

        Notification notification = new Notification();

        notification.setCreatedAt(LocalDateTime.now());

        if (content.getTeam() != null &&
                content.getTeam().getTeamLeader() != null) {

            notification.setUser(
                    content.getTeam().getTeamLeader()
            );

            notification.setContentId(content.getId());

            notification.setMessage(
                    content.getCreatedBy().getName()
                            + " submitted new content: "
                            + content.getTitle()
            );

            notificationRepository.save(notification);

        } else {

            List<User> admins =
                    userRepository.findByRole(Role.ADMIN);

            for (User admin : admins) {

                Notification adminNotification =
                        new Notification();

                adminNotification.setUser(admin);

                adminNotification.setCreatedAt(
                        LocalDateTime.now()
                );

                adminNotification.setContentId(content.getId());

                adminNotification.setMessage(
                        content.getCreatedBy().getName()
                                + " submitted new content: "
                                + content.getTitle()
                );

                notificationRepository.save(
                        adminNotification
                );
            }
        }
    }
}