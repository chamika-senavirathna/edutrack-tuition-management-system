<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="${invoice != null ? 'Edit Invoice' : 'New Invoice'}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="row">
                <div class="col-lg-8">
                    <form method="post" action="${pageContext.request.contextPath}${invoice != null ? '/fees/edit' : '/fees/add'}">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <c:if test="${invoice != null}"><input type="hidden" name="id" value="${invoice.id}"></c:if>
                        <div class="card"><div class="card-header">Invoice Details</div>
                            <div class="card-body row g-3">
                                <c:if test="${invoice == null}">
                                    <div class="col-md-6"><label class="form-label required-label">Student</label>
                                        <select class="form-select" name="studentId" required>
                                            <option value="-1">Select…</option>
                                            <c:forEach items="${students}" var="s">
                                                <option value="${s.id}">${s.regNo} — ${s.fullName()}</option>
                                            </c:forEach>
                                        </select>
                                        <div class="form-text">A confirmed active enrolment is required (integration rule).</div></div>
                                    <div class="col-md-6"><label class="form-label">Class</label>
                                        <select class="form-select" name="classId">
                                            <option value="-1">Select…</option>
                                            <c:forEach items="${classes}" var="c"><option value="${c.id}">${c.name}</option></c:forEach>
                                        </select></div>
                                </c:if>
                                <div class="col-md-6"><label class="form-label required-label">Term</label>
                                    <select class="form-select" name="termId" ${invoice != null ? 'disabled' : 'required'}>
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${terms}" var="t">
                                            <option value="${t.id}" ${invoice != null && invoice.termId == t.id ? 'selected' : ''}>${t.name}</option>
                                        </c:forEach>
                                    </select></div>
                                <div class="col-md-3"><label class="form-label required-label">Amount (LKR)</label>
                                    <input type="number" step="0.01" min="0.01" class="form-control" name="amount"
                                           value="${invoice.amount}" required></div>
                                <div class="col-md-3"><label class="form-label">Discount (LKR)</label>
                                    <input type="number" step="0.01" min="0" class="form-control" name="discount"
                                           value="${invoice.discount != null ? invoice.discount : 0}"></div>
                                <div class="col-md-3"><label class="form-label">Due date</label>
                                    <input type="date" class="form-control" name="dueDate" value="${invoice.dueDate}"></div>
                                <div class="col-12"><label class="form-label">Remarks</label>
                                    <input class="form-control" name="remarks" value="${invoice.remarks}"></div>
                            </div>
                        </div>
                        <c:if test="${invoice == null || invoice.status != 'PAID'}">
                            <button class="btn btn-primary mt-3">${invoice != null ? 'Save Changes' : 'Create Invoice'}</button>
                        </c:if>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
