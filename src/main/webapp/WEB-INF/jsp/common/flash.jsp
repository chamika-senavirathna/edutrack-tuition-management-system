<%@ page contentType="text/html; charset=UTF-8" %>
<div class="et-flash">
    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${flashSuccess}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${flashError}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
</div>
