Web Service Monitor Tool - report generated at: ${.now}

Web Service HttpStatus Response - session tracked with correlation id: ${results.correlationId}

<#list results.statusChecks as url, status>
<#if status == "200">
* ${url} -> (${status}) -> passed -> :-)
<#elseif status == "418">
* ${url} -> (UNKNOWN) -> FAILED -> :-O
<#else>
* ${url} -> (${status}) -> FAILED -> :-(
</#if>
</#list>

- UNKNOWN status indicates that the website did not respond with a valid Http Status code.