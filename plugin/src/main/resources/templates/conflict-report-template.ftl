## Dependency conflict report

<#if !data?has_content>
✅ Everything is up-to-date
<#else>
| Dependency  | Status | Versions |
| --- | --- | --- |
<#list data as conflict>
| ${conflict.library} | ${conflict.level} | ${conflict.versions} |
</#list>
</#if>

<br>