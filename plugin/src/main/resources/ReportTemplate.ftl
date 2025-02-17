## Dependency Report

<#if !dependencies?has_content>
✅ Everything is up-to-date
<#else>
| Dependency Name | Status | Current Version | Latest Version |
| --- | --- | --- | --- |
<#list dependencies as dep>
| ${dep.name} | ${dep.status} | ${dep.currentVersion} | ${dep.latestVersion} |
</#list>
</#if>

<br>

```Plugin version ${version}```