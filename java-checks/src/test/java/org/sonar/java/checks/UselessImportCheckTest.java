/*
 * SonarQube Java
 * Copyright (C) 2012-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.FilesUtils;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

import static org.sonar.java.checks.verifier.TestUtils.nonCompilingTestSourcesPath;
import static org.sonar.java.checks.verifier.TestUtils.testSourcesPath;

class UselessImportCheckTest {

  @Test
  void detected_with_package() {
    JavaCheckVerifier.newVerifier()
      .onFile(nonCompilingTestSourcesPath("checks/UselessImportCheck/WithinPackage.java"))
      .withCheck(new UselessImportCheck())
      .verifyIssues();
  }

  @Test
  void detected_within_package_info() {
    JavaCheckVerifier.newVerifier()
      .onFile(testSourcesPath("checks/UselessImportCheck/package-info.java"))
      .withCheck(new UselessImportCheck())
      .verifyIssues();
  }

  @Test
  void no_semantic() {
    JavaCheckVerifier.newVerifier()
      .onFile(nonCompilingTestSourcesPath("checks/UselessImportCheck/NoSemanticWithPackage.java"))
      .withCheck(new UselessImportCheck())
      .withoutSemantic()
      .verifyNoIssues();
  }

  @Test
  void detected_without_package() {
    JavaCheckVerifier.newVerifier()
      .onFile(nonCompilingTestSourcesPath("checks/UselessImportCheck/WithoutPackage.java"))
      .withCheck(new UselessImportCheck())
      .verifyIssues();
  }

  @Test
  void with_module() {
    JavaCheckVerifier.newVerifier()
      .onFile(nonCompilingTestSourcesPath("module/module-info.java"))
      .withCheck(new UselessImportCheck())
      .verifyNoIssues();
  }

  @Test
  void intersection_type() {
    JavaCheckVerifier.newVerifier()
      .onFile(testSourcesPath("checks/UselessImportCheck/IntersectionCase.java"))
      .withCheck(new UselessImportCheck())
      .verifyIssues();
  }

  @Test
  void useless_import_of_nested_class() {
    List<File> classPath = FilesUtils.getClassPath(FilesUtils.DEFAULT_TEST_JARS_DIRECTORY);
    // Add "UselessImportCheckWithNestedClass" to class path to be able to resolve the import.
    classPath.add(new File("../java-checks-test-sources/target/classes"));

    JavaCheckVerifier.newVerifier()
      .onFile(testSourcesPath("checks/UselessImportCheck/subpackage/UselessImportCheckImportingNested.java"))
      .withClassPath(classPath)
      .withCheck(new UselessImportCheck())
      .verifyIssues();

    JavaCheckVerifier.newVerifier()
      .onFile(testSourcesPath("checks/UselessImportCheck/subpackage/UselessImportCheckImportingNestedOrder.java"))
      .withClassPath(classPath)
      .withCheck(new UselessImportCheck())
      .verifyIssues();
  }
}
