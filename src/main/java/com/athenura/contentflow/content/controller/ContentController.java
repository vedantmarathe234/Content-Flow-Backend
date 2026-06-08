package com.athenura.contentflow.content.controller;

import com.athenura.contentflow.commons.enums.ContentStatus;
import com.athenura.contentflow.content.dto.*;
import com.athenura.contentflow.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContentResponse> createContent(
            @RequestPart("data") CreateContentRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Principal principal) {
        return ResponseEntity.ok(contentService.createContent(request, file, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ContentResponse>> getAllContent() {
        return ResponseEntity.ok(contentService.getAllContent());
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse>
    getDashboardStats() {

        return ResponseEntity.ok(
                contentService.getDashboardStats()
        );
    }

    @GetMapping("/dashboard/my-stats")
    public ResponseEntity<UserDashboardResponse>
    getMyDashboardStats(
            Principal principal
    ) {

        return ResponseEntity.ok(
                contentService.getMyDashboardStats(
                        principal.getName()
                )
        );
    }

    @GetMapping("/dashboard/team/{teamId}")
    public ResponseEntity<DashboardStatsResponse> getTeamDashboard(
            @PathVariable Long teamId,
            Principal principal
    ) {

        return ResponseEntity.ok(
                contentService.getTeamDashboardStats(
                        teamId,
                        principal.getName()
                )
        );
    }


    @GetMapping("/my")
    public ResponseEntity<List<ContentResponse>> getMyContents(Principal principal) {
        return ResponseEntity.ok(contentService.getMyContents(principal.getName()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ContentResponse> getContentById(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.getContentById(id));
    }


    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ContentResponse> updateContent(
            @PathVariable Long id,
            @RequestPart("data") CreateContentRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Principal principal
    ) {
        return ResponseEntity.ok(
                contentService.updateAndResubmit(
                        id,
                        request,
                        file,
                        principal.getName()
                )
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteContent(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(contentService.deleteContent(id, principal.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approveContent(
            @PathVariable Long id,
            Principal principal
    ) {

        return ResponseEntity.ok(
                contentService.approveContent(
                        id,
                        principal.getName()
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse> rejectContent(
            @PathVariable Long id,
            @RequestBody RejectContentRequest request,
            Principal principal
    ) {

        return ResponseEntity.ok(
                contentService.rejectContent(
                        id,
                        request,
                        principal.getName()
                )
        );
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<ContentResponse>> getByStatus(@PathVariable ContentStatus status) {
        return ResponseEntity.ok(contentService.getContentByStatus(status));
    }


    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<ContentResponse>> getByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(contentService.getContentByDepartment(deptId));
    }

    @PreAuthorize("hasRole('INTERN')")
    @PutMapping("/{id}/leader-approve")
    public ResponseEntity<ApiResponse> approveContentByLeader(

            @PathVariable Long id,
            Principal principal
    ) {
        return ResponseEntity.ok(contentService.approveContentByLeader(id, principal.getName()));
    }

    @PreAuthorize("hasRole('INTERN')")
    @PutMapping("/{id}/leader-reject")
    public ResponseEntity<ApiResponse> rejectContentByLeader(

            @PathVariable Long id,
            @RequestBody RejectContentRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(
                contentService.rejectContentByLeader(
                        id,
                        request,
                        principal.getName()
                )
        );
    }
}