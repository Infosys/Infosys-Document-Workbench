<%@ page language='java' contentType='text/html; charset=ISO-8859-1'
	pageEncoding='ISO-8859-1'%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>
<html>

<head>
    <link href='/docwbservice/webjars/bootstrap/4.2.1/css/bootstrap.min.css' rel='stylesheet' />
    <script type='text/javascript' src='/docwbservice/webjars/jquery/3.0.0/jquery.min.js'></script>
    <script type='text/javascript' src='/docwbservice/webjars/bootstrap/4.2.1/js/bootstrap.min.js'></script>
    <title>Document Workbench Service</title>
</head>

<body>
    <br />
    <br />
    <br />
    <div style="width: 650px; margin: 0 auto;">
        <br />
        <br />
        <h2 align="center">Document Workbench Service</h2>
        <h3 align="center">Developer Access</h3>
        <br />
        <br />
        <c:choose>
            <c:when test="${empty requestScope.exception}">
            	<h6 align="center">To login please use your company User ID only without @*.com</h6>
        		<br />
                <form name='f' action="dologin" method='POST'>
                    <div class="section ">
                        <div style="text-align: center;">
                            <input class="input" placeholder=" User ID " autocomplete="off" type="text" required
                                name="username">
                        </div>
                    </div>
                    <br />
                    <div class="section">
                        <div style="text-align: center;">
                            <input class="input" placeholder=" Password " autocomplete="off" type="password" required
                                name="password">
                        </div>
                    </div>
                    <br />
                    <div class="section">
                        <div style="text-align: center;">
                            <input class="input" placeholder=" TenantId " autocomplete="off" type="text" required
                                name="tenantId">
                        </div>
                    </div>
                    <div class="section">
                        <div style="text-align: center; padding-top: 20px;">
                            <input type="submit" class="button" value="LOGIN">
                        </div>
                    </div>
                </form>
            </c:when>
            <c:otherwise>
                <p style="color: red;" align="center">
                    Error Message :
                    <%=request.getAttribute("exception")%>
                </p>
                <p align="center">
                    <a href='login.html'>Try again</a>
                </p>
            </c:otherwise>
        </c:choose>
    </div>
</body>

</html>