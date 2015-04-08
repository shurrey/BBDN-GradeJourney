<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page import="bbdn.lis.gradejourney.GradeJourney,
				javax.xml.soap.SOAPMessage" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Grade Journey Sample</title>
</head>
<body>
	<% 
		if ("POST".equalsIgnoreCase(request.getMethod())) {
			
			String methodName = request.getParameter("method_name");%>
			
			<%=GradeJourney.invoke(methodName)%>	
			
			<form method="get" enctype="application/x-www-form-urlencoded" action="index.jsp" name="method_display">
				<br />
				<input type="submit" value="Back">
			</form>
			
		<%} else { %>
			<form method="post" enctype="application/x-www-form-urlencoded" action="index.jsp" name="method_selection">
				<h1>Blackboard Grade Journey Sample</h1>
				<p>Please select the radio button that corresponds to the call you'd like to make and click submit to display the results.</p> 
				<input type="radio" name="method_name" value="replaceLineItemRequest" /> Create Gradebook Column - ims:replaceLineItemRequest<br />
				<input type="radio" name="method_name" value="manualReplaceLineItemRequest" /> Create Manual Gradebook Column - ims:replaceLineItem with ims:extension for PointsPossible <br />
				<input type="radio" name="method_name" value="readResultIdsForCourseSectionRequest" /> Read Result Ids For Course Section - readResultIdsForCourseSectionRequest <br />
				<input type="radio" name="method_name" value="readResultsRequest" /> Read Results - ims:readResultsRequest <br />
				<br />
				<input type="submit" value="Submit">
			</form>
	<%	}%>
</body>
</html>