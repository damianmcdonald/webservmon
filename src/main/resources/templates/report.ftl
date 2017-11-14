<div style="padding: 10px;">
    <div style="padding: 10px;">
        <h2>Web Service Monitor Tool - report generated at: ${.now}</h2>
    </div>
    <hr/>
    <div style="padding: 10px;">
        <h3>Web Service HttpStatus Response</h3>
        <table style="border-collapse: collapse;">
            <thead>
            <tr style="border: 1px solid black;">
                <th style="border: 1px solid black; padding: 5px;">Service Url</th>
                <th style="border: 1px solid black; padding: 5px;">HttpStatus</th>
            </tr>
            </thead>
            <tbody>
            <#list results as url, status>
            <tr style="border: 1px solid black;">
                <td style="border: 1px solid black; padding: 5px;">${url}</td>
                <td style="border: 1px solid black; padding: 5px;">
                    <#if status == "200">
                        <span style="color:green">${status}</span>
                    <#else>
                        <span style="color:red">${status}</span>
                    </#if>
                </td>
            </tr>
            </#list>
            </tbody>
        </table>

    </div>
</div>