package com.edutrack.servlet;

import com.edutrack.model.Notice;
import com.edutrack.model.TeachingMaterial;
import com.edutrack.model.User;
import com.edutrack.service.RegistrationService;
import com.edutrack.service.TimetableService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Communication controller (Member 02): notices and teaching materials
 * (absorbed from the former seventh module per the final six-member allocation).
 */
@WebServlet({"/notices", "/notices/add", "/notices/edit", "/notices/archive", "/notices/view",
        "/materials", "/materials/add", "/materials/edit", "/materials/archive"})
public class NoticeServlet extends BaseServlet {

    private final TimetableService timetableService = new TimetableService();
    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        switch (path) {
            case "/notices" -> {
                // Staff see the management list; students/parents see audience-filtered notices.
                boolean staff = user.hasRole("ADMIN") || user.hasRole("ACADEMIC_COORDINATOR")
                        || user.hasRole("PRINCIPAL") || user.hasRole("TEACHER") || user.hasRole("FINANCE");
                if (staff) {
                    String q = stringParam(request, "q");
                    String category = stringParam(request, "category");
                    String status = stringParam(request, "status");
                    request.setAttribute("notices", timetableService.searchNotices(q, category, null, status, 0, 50));
                    request.setAttribute("q", q);
                    request.setAttribute("category", category);
                    request.setAttribute("status", status);
                } else {
                    List<Long> classIds = scopedClasses(user).stream().map(com.edutrack.model.ClassRoom::getId).toList();
                    request.setAttribute("notices", timetableService.noticesForViewer(user, user.getStudentId(),
                            user.getTeacherId(), classIds, 50));
                }
                view(request, response, "communication/notices.jsp");
            }
            case "/notices/view" -> {
                Notice notice = timetableService.findNotice(longParam(request, "id", -1));
                if (notice == null) {
                    redirect(request, response, "/notices");
                    return;
                }
                var ownClasses = scopedClasses(user);
                if (ownClasses != null && timetableService.noticesForViewer(user, user.getStudentId(), null,
                        ownClasses.stream().map(com.edutrack.model.ClassRoom::getId).toList(), Integer.MAX_VALUE)
                        .stream().noneMatch(n -> n.getId().equals(notice.getId())))
                    throw new com.edutrack.exception.AuthorizationException("This notice is not addressed to your account.");
                request.setAttribute("notice", notice);
                view(request, response, "communication/notice_view.jsp");
            }
            case "/notices/add", "/notices/edit" -> {
                if ("/notices/edit".equals(path)) {
                    request.setAttribute("notice", timetableService.findNotice(longParam(request, "id", -1)));
                }
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("subjects", timetableService.subjects(false));
                request.setAttribute("roles", new com.edutrack.service.UserService().allRoles());
                view(request, response, "communication/notice_form.jsp");
            }
            case "/materials" -> {
                boolean staff = user.hasRole("ADMIN") || user.hasRole("ACADEMIC_COORDINATOR")
                        || user.hasRole("PRINCIPAL") || user.hasRole("TEACHER");
                if (staff && user.getTeacherId() == null) {
                    request.setAttribute("materials", timetableService.searchMaterials(
                            stringParam(request, "q"), longParam(request, "classId", -1) == -1 ? null : longParam(request, "classId", -1),
                            null, stringParam(request, "status"), 0, 50));
                } else if (staff) {
                    // Teachers see materials of their classes plus anything they uploaded.
                    List<Long> classIds = timetableService.classesOfTeacher(user.getTeacherId(), null)
                            .stream().map(com.edutrack.model.ClassRoom::getId).toList();
                    request.setAttribute("materials", timetableService.materialsForClasses(classIds));
                } else {
                    List<Long> classIds = scopedClasses(user).stream().map(com.edutrack.model.ClassRoom::getId).toList();
                    request.setAttribute("materials", timetableService.materialsForClasses(classIds));
                }
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                view(request, response, "communication/materials.jsp");
            }
            case "/materials/add", "/materials/edit" -> {
                if ("/materials/edit".equals(path)) {
                    var material = timetableService.findMaterial(longParam(request, "id", -1));
                    if (material == null) { response.sendError(404); return; }
                    if (user.hasRole("TEACHER") && !user.hasRole("ADMIN") && !user.hasRole("PRINCIPAL")
                            && !user.hasRole("ACADEMIC_COORDINATOR") && !user.getId().equals(material.getUploadedBy()))
                        throw new com.edutrack.exception.AuthorizationException("You can only edit your own materials.");
                    request.setAttribute("material", material);
                }
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("subjects", timetableService.subjects(false));
                view(request, response, "communication/material_form.jsp");
            }
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/notices/add" -> {
                    Notice notice = readNotice(request);
                    boolean publish = "publish".equals(stringParam(request, "submitAction"));
                    timetableService.createNotice(user, notice, publish);
                    com.edutrack.util.Flash.success(request.getSession(),
                            publish ? "Notice published to its audience." : "Notice saved as draft.");
                    redirect(request, response, "/notices");
                }
                case "/notices/edit" -> {
                    Notice notice = readNotice(request);
                    notice.setId(longParam(request, "id", -1));
                    boolean publish = "publish".equals(stringParam(request, "submitAction"));
                    timetableService.updateNotice(user, notice, publish);
                    com.edutrack.util.Flash.success(request.getSession(), "Notice updated.");
                    redirect(request, response, "/notices");
                }
                case "/notices/archive" -> {
                    timetableService.archiveNotice(user, longParam(request, "id", -1));
                    com.edutrack.util.Flash.success(request.getSession(), "Notice archived.");
                    redirect(request, response, "/notices");
                }
                case "/materials/add", "/materials/edit" -> {
                    TeachingMaterial m = new TeachingMaterial();
                    m.setClassId(longParam(request, "classId", -1) == -1 ? null : longParam(request, "classId", -1));
                    m.setSubjectId(longParam(request, "subjectId", -1) == -1 ? null : longParam(request, "subjectId", -1));
                    m.setUploadedBy(user.getId());
                    m.setTitle(stringParam(request, "title"));
                    m.setDescription(stringParam(request, "description"));
                    // Demo-safe: stores a reference link instead of a binary upload.
                    m.setFilePath(stringParam(request, "link"));
                    m.setFileName(stringParam(request, "link"));
                    if ("/materials/edit".equals(path)) {
                        m.setId(longParam(request, "id", -1));
                        timetableService.updateMaterial(user, m);
                    } else timetableService.createMaterial(user, m);
                    com.edutrack.util.Flash.success(request.getSession(), "Teaching material shared.");
                    redirect(request, response, "/materials");
                }
                case "/materials/archive" -> {
                    timetableService.archiveMaterial(user, longParam(request, "id", -1));
                    com.edutrack.util.Flash.success(request.getSession(), "Material archived.");
                    redirect(request, response, "/materials");
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            handle(request, response, path.startsWith("/materials") ? "/materials" : "/notices", e);
        }
    }

    private Notice readNotice(HttpServletRequest request) {
        Notice n = new Notice();
        n.setTitle(stringParam(request, "title"));
        n.setContent(stringParam(request, "content"));
        n.setCategory(stringParam(request, "category"));
        n.setAudienceType(stringParam(request, "audienceType"));
        n.setChannel(stringParam(request, "channel"));
        n.setPriority(stringParam(request, "priority"));
        n.setCreatedBy(currentUser(request).getId());
        Long ref = longParam(request, "audienceRefId", -1);
        n.setAudienceRefId(ref == -1 ? null : ref);
        if ("ROLE".equals(n.getAudienceType()) && ref != -1) {
            n.setAudienceRefId(ref); // role notices store the role name textually
        }
        return n;
    }
}
