Web Service Monitor Tool - report generated at: ${.now}

FTP status check results for DESIGN : correlation id = ${results.correlationId}

<#if results.successMilestones?has_content>
Success milestones achieved:

    <#list results.successMilestones as successMessage>
        ${successMessage}
    </#list>
</#if>

<#if results.exception?has_content>
Exceptions which occurred during the status check:
    ${results.exception}
</#if>

<#if results.errorsDownload?size != 0>
Error messages from FTP downloading stage:

    <#list results.errorsDownload as errorDownloadMessage>
        ${errorDownloadMessage}
    </#list>
</#if>

<#if results.errorsUnzip?size != 0>
Error messages from FTP Unzipping stage:

    <#list results.errorsUnzip as errorUnzipMessage>
        ${errorUnzipMessage}
    </#list>
</#if>

<#if results.errorsXmlValidation?size != 0>
Error messages from FTP XML Validation stage:

    <#list results.errorsXmlValidation as errorValidateXmlMessage>
        ${errorValidateXmlMessage}
    </#list>
</#if>