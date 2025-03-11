# Tricky dependencies to get data

| Name                                           | Status | Details                                         |
|------------------------------------------------|--------|-------------------------------------------------|
| com.google.guava:guava:31.1-jre                | Solved | Getting version from parent                     |
| androidx.lifecycle:lifecycle-runtime:2.6.1     | Solved | Children version in bracers []                  |
| com.squareup.okhttp3:okhttp:3.14.9             | Solved | Getting version from parent                     |
| log4j:log4j:1.2.12                             | Solved | Pom Namespace missing                           |
| org.apache.httpcomponents:httpclient:4.0.1     | Solved | Version uses key from parent properties         |
| org.jetbrains.kotlin:kotlin-stdlib:1.3.0-rc-57 | Solved | Pom doesn't exist in mvn, return empty children |
| com.google.dagger:hilt-android:2.52            | Solved | Getting version from project                    |
| org.glassfish:javax.annotation:10.0-b28        | Solved | Need to take from grandparent version           |  
| com.google.guava:guava:27.1-android            | Solved | Version in parent dependencyManagement          |