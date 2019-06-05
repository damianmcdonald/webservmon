<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
<head></head>
<body>
<div style="padding: 10px;">
    <div style="padding: 10px;">
        <h2>Web Service Monitor Tool - report generated at: ${.now}</h2>
    </div>
    <hr/>
    <div style="padding: 10px;">
        <h3>Web Service HttpStatus Response</h3>
        <table id="results-table" style="border-collapse: collapse;">
            <thead>
            <tr style="border: 1px solid black;">
                <th style="border: 1px solid black; padding: 5px;">Service Url</th>
                <th style="border: 1px solid black; padding: 5px;">HttpStatus</th>
                <th style="border: 1px solid black; padding: 5px;">Emoji</th>
            </tr>
            </thead>
            <tbody>
            <#list results as url, status>
            <tr style="border: 1px solid black;">
                <td style="border: 1px solid black; padding: 5px;">${url}</td>
                    <#if status == "200">
                        <td class="http-success" style="border: 1px solid black; padding: 5px;">
                            <span style="color:green;">${status}</span>
                        </td>
                        <td style="border: 1px solid black; padding: 5px;">
                            <span style="color:green;">:-)</span>
                        </td>
                    <#elseif status == "418">
                        <td class="http-unknown" style="border: 1px solid black; padding: 5px;">
                            <span style="color:red;">UNKNOWN</span>
                        </td>
                        <td style="border: 1px solid black; padding: 5px;">
                            <span style="color:red;">:-O</span>
                        </td>
                    <#else>
                        <td class="http-error" style="border: 1px solid black; padding: 5px;">
                            <span style="color:red;">${status}</span>
                        </td>
                        <td style="border: 1px solid black; padding: 5px;">
                            <span style="color:red;">:-(</span>
                        </td>
                    </#if>
                </td>
            </tr>
            </#list>
            </tbody>
        </table>

		<#list results as url, status>
			<#if status == "418">
				<p class="http-service-errors" style="color:red;"><span style="font-weight: bold;">*</span> UNKNOWN status indicates that the website did not respond with a valid Http Status code.</p>
			</#if>
		</#list>
    </div>
</div>
</body>
</html>