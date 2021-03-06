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
package org.sonar.java.regex.ast;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.java.regex.RegexParseResult;
import org.sonar.java.regex.SyntaxError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sonar.java.regex.RegexParserTestUtils.assertEdge;
import static org.sonar.java.regex.RegexParserTestUtils.assertKind;
import static org.sonar.java.regex.RegexParserTestUtils.assertListElements;
import static org.sonar.java.regex.RegexParserTestUtils.assertLocation;
import static org.sonar.java.regex.RegexParserTestUtils.assertCharacter;
import static org.sonar.java.regex.RegexParserTestUtils.assertSingleEdge;
import static org.sonar.java.regex.RegexParserTestUtils.assertSuccessfulParse;
import static org.sonar.java.regex.RegexParserTestUtils.assertType;
import static org.sonar.java.regex.RegexParserTestUtils.parseRegex;

class CurlyBraceQuantifierTest {

  @Test
  void testCurlyBracedQuantifier() {
    RegexTree regex = assertSuccessfulParse("x{23,42}");
    RepetitionTree repetition = assertType(RepetitionTree.class, regex);
    assertKind(RegexTree.Kind.REPETITION, repetition);
    assertCharacter('x', repetition.getElement());
    CurlyBraceQuantifier quantifier = assertType(CurlyBraceQuantifier.class, repetition.getQuantifier());
    assertLocation(2, 4, quantifier.getMinimumRepetitionsToken());
    assertLocation(4, 5, quantifier.getCommaToken());
    assertLocation(5, 7, quantifier.getMaximumRepetitionsToken());
    assertEquals(23, quantifier.getMinimumRepetitions(), "Lower bound should be 23.");
    assertEquals(42, quantifier.getMaximumRepetitions(), "Upper bound should be 42.");
    assertFalse(quantifier.isOpenEnded(), "Quantifier should not be open ended.");
    assertFalse(quantifier.isFixed(), "Quantifier should not be marked as only having a single number.");
    assertEquals(Quantifier.Modifier.GREEDY, quantifier.getModifier(), "Quantifier should be greedy.");

    testAutomaton(repetition, false);
  }

  @Test
  void testCurlyBracedQuantifierWithNoUpperBound() {
    RegexTree regex = assertSuccessfulParse("x{42,}");
    RepetitionTree repetition = assertType(RepetitionTree.class, regex);
    assertKind(RegexTree.Kind.REPETITION, repetition);
    assertCharacter('x', repetition.getElement());
    CurlyBraceQuantifier quantifier = assertType(CurlyBraceQuantifier.class, repetition.getQuantifier());
    assertEquals(42, quantifier.getMinimumRepetitions(), "Lower bound should be 42.");
    assertNull(quantifier.getMaximumRepetitions(), "Quantifier should be open ended.");
    assertTrue(quantifier.isOpenEnded(), "Quantifier should be open ended.");
    assertFalse(quantifier.isFixed(), "Quantifier should not be marked as only having a single number.");
    assertEquals(Quantifier.Modifier.GREEDY, quantifier.getModifier(), "Quantifier should be greedy.");

    testAutomaton(repetition, false);
  }

  @Test
  void testFixedCurlyBracedQuantifier() {
    RegexTree regex = assertSuccessfulParse("x{42}");
    RepetitionTree repetition = assertType(RepetitionTree.class, regex);
    assertCharacter('x', repetition.getElement());
    CurlyBraceQuantifier quantifier = assertType(CurlyBraceQuantifier.class, repetition.getQuantifier());
    assertEquals(42, quantifier.getMinimumRepetitions(), "Lower bound should be 42.");
    assertEquals(42, quantifier.getMaximumRepetitions(), "Upper bound should be the same as lower bound.");
    assertFalse(quantifier.isOpenEnded(), "Quantifier should not be open ended.");
    assertTrue(quantifier.isFixed(), "Quantifier should be marked as only having a single number.");
    assertEquals(Quantifier.Modifier.GREEDY, quantifier.getModifier(), "Quantifier should be greedy.");

    testAutomaton(repetition, false);
  }

  @Test
  void testReluctantCurlyBracedQuantifier() {
    RegexTree regex = assertSuccessfulParse("x{23,42}?");
    RepetitionTree repetition = assertType(RepetitionTree.class, regex);
    assertCharacter('x', repetition.getElement());
    CurlyBraceQuantifier quantifier = assertType(CurlyBraceQuantifier.class, repetition.getQuantifier());
    assertEquals(23, quantifier.getMinimumRepetitions(), "Lower bound should be 23.");
    assertEquals(42, quantifier.getMaximumRepetitions(), "Upper bound should be 42.");
    assertEquals(Quantifier.Modifier.RELUCTANT, quantifier.getModifier(), "Quantifier should be reluctant.");

    testAutomaton(repetition, true);
  }

  @Test
  void testPossessiveCurlyBracedQuantifier() {
    RegexTree regex = assertSuccessfulParse("x{23,42}+");
    RepetitionTree repetition = assertType(RepetitionTree.class, regex);
    assertCharacter('x', repetition.getElement());
    CurlyBraceQuantifier quantifier = assertType(CurlyBraceQuantifier.class, repetition.getQuantifier());
    assertEquals(23, quantifier.getMinimumRepetitions(), "Lower bound should be 23.");
    assertEquals(42, quantifier.getMaximumRepetitions(), "Upper bound should be 42.");
    assertEquals(Quantifier.Modifier.POSSESSIVE, quantifier.getModifier(), "Quantifier should be possessive.");

    testAutomaton(repetition, false);
  }

  @Test
  void testOneOneCurlyBracedQuantifier() {
    RegexTree regex = assertSuccessfulParse("x{1,1}");
    RepetitionTree repetition = assertType(RepetitionTree.class, regex);
    RegexTree x = repetition.getElement();
    assertCharacter('x', x);
    CurlyBraceQuantifier quantifier = assertType(CurlyBraceQuantifier.class, repetition.getQuantifier());
    assertEquals(1, quantifier.getMinimumRepetitions(), "Lower bound should be 1.");
    assertEquals(1, quantifier.getMaximumRepetitions(), "Upper bound should be 1.");
    assertEquals(Quantifier.Modifier.GREEDY, quantifier.getModifier(), "Quantifier should be possessive.");

    FinalState finalState = assertType(FinalState.class, repetition.continuation());
    assertEquals(AutomatonState.TransitionType.EPSILON, repetition.incomingTransitionType());
    assertSingleEdge(repetition, x, AutomatonState.TransitionType.CHARACTER);
    assertSingleEdge(x, finalState, AutomatonState.TransitionType.EPSILON);
  }

  @Test
  void testZeroZeroCurlyBracedQuantifier() {
    RegexTree regex = assertSuccessfulParse("x{0,0}");
    RepetitionTree repetition = assertType(RepetitionTree.class, regex);
    RegexTree x = repetition.getElement();
    assertCharacter('x', x);
    CurlyBraceQuantifier quantifier = assertType(CurlyBraceQuantifier.class, repetition.getQuantifier());
    assertEquals(0, quantifier.getMinimumRepetitions(), "Lower bound should be 0.");
    assertEquals(0, quantifier.getMaximumRepetitions(), "Upper bound should be 0.");
    assertEquals(Quantifier.Modifier.GREEDY, quantifier.getModifier(), "Quantifier should be possessive.");

    FinalState finalState = assertType(FinalState.class, repetition.continuation());
    assertEquals(AutomatonState.TransitionType.EPSILON, repetition.incomingTransitionType());
    assertSingleEdge(repetition, finalState, AutomatonState.TransitionType.EPSILON);
  }

  @Test
  void testCurlyBracedQuantifierWithNonNumber() {
    RegexParseResult result = parseRegex("x{a}");
    assertEquals(1, result.getSyntaxErrors().size(), "Expected exactly one error.");
    SyntaxError error = result.getSyntaxErrors().get(0);
    assertEquals("Expected integer, but found 'a'", error.getMessage(), "Error should have the right message.");
    assertEquals("a", error.getOffendingSyntaxElement().getText(), "Error should complain about the correct part of the regex.");
    List<Location> locations = error.getLocations();
    assertEquals(1, locations.size(), "Error should only have one location.");
    assertEquals(new IndexRange(2, 3), locations.get(0).getIndexRange(), "Error should have the right location.");
  }

  @Test
  void testCurlyBracedQuantifierWithJunkAfterNumber() {
    RegexParseResult result = parseRegex("x{1a}");
    assertEquals(1, result.getSyntaxErrors().size(), "Expected exactly one error.");
    SyntaxError error = result.getSyntaxErrors().get(0);
    assertEquals("Expected ',' or '}', but found 'a'", error.getMessage(), "Error should have the right message.");
    assertEquals("a", error.getOffendingSyntaxElement().getText(), "Error should complain about the correct part of the regex.");
    List<Location> locations = error.getLocations();
    assertEquals(1, locations.size(), "Error should only have one location.");
    assertEquals(new IndexRange(3, 4), locations.get(0).getIndexRange(), "Error should have the right location.");
  }

  @Test
  void testCurlyBracedQuantifierWithJunkAfterComma() {
    RegexParseResult result = parseRegex("x{1,a}");
    assertEquals(1, result.getSyntaxErrors().size(), "Expected exactly one error.");
    SyntaxError error = result.getSyntaxErrors().get(0);
    assertEquals("Expected integer or '}', but found 'a'", error.getMessage(), "Error should have the right message.");
    assertEquals("a", error.getOffendingSyntaxElement().getText(), "Error should complain about the correct part of the regex.");
    List<Location> locations = error.getLocations();
    assertEquals(1, locations.size(), "Error should only have one location.");
    assertEquals(new IndexRange(4, 5), locations.get(0).getIndexRange(), "Error should have the right location.");
  }

  @Test
  void testCurlyBracedQuantifierWithJunkAfterSecondNumber() {
    RegexParseResult result = parseRegex("x{1,2a}");
    assertEquals(1, result.getSyntaxErrors().size(), "Expected exactly one error.");
    SyntaxError error = result.getSyntaxErrors().get(0);
    assertEquals("Expected '}', but found 'a'", error.getMessage(), "Error should have the right message.");
    assertEquals("a", error.getOffendingSyntaxElement().getText(), "Error should complain about the correct part of the regex.");
    List<Location> locations = error.getLocations();
    assertEquals(1, locations.size(), "Error should only have one location.");
    assertEquals(new IndexRange(5, 6), locations.get(0).getIndexRange(), "Error should have the right location.");
  }

  @Test
  void testCurlyBracedQuantifierWithMissingClosingBrace() {
    RegexParseResult result = parseRegex("x{1,2");
    assertEquals(1, result.getSyntaxErrors().size(), "Expected exactly one error.");
    SyntaxError error = result.getSyntaxErrors().get(0);
    assertEquals("Expected '}', but found the end of the regex", error.getMessage(), "Error should have the right message.");
    assertEquals("", error.getOffendingSyntaxElement().getText(), "Error should complain about the correct part of the regex.");
    List<Location> locations = error.getLocations();
    assertEquals(1, locations.size(), "Error should only have one location.");
    assertEquals(new IndexRange(5, 5), locations.get(0).getIndexRange(), "Error should have the right location.");
    assertTrue(locations.get(0).getIndexRange().isEmpty(), "Error location should be empty range at end of regex.");
  }

  @Test
  void testCurlyBracedQuantifierWithoutOperand() {
    RegexParseResult result = parseRegex("{1,2}");
    assertEquals(1, result.getSyntaxErrors().size(), "Expected exactly one error.");
    SyntaxError error = result.getSyntaxErrors().get(0);
    assertEquals("Unexpected quantifier '{1,2}'", error.getMessage(), "Error should have the right message.");
    assertEquals("{1,2}", error.getOffendingSyntaxElement().getText(), "Error should complain about the correct part of the regex.");
    List<Location> locations = error.getLocations();
    assertEquals(1, locations.size(), "Error should only have one location.");
    assertEquals(new IndexRange(0, 5), locations.get(0).getIndexRange(), "Error should have the right location.");
    assertFalse(locations.get(0).getIndexRange().isEmpty(), "Error location should not be empty range.");
  }

  @Test
  void testCurlyBracedQuantifierWithoutOperandInGroup() {
    RegexParseResult result = parseRegex("({1,2})");
    assertEquals(1, result.getSyntaxErrors().size(), "Expected exactly one error.");
    SyntaxError error = result.getSyntaxErrors().get(0);
    assertEquals("Unexpected quantifier '{1,2}'", error.getMessage(), "Error should have the right message.");
    assertEquals(error.getMessage(), error.toString(), "SyntaxError.toString() should equal the error message.");
    assertEquals("{1,2}", error.getOffendingSyntaxElement().getText(), "Error should complain about the correct part of the regex.");
    List<Location> locations = error.getLocations();
    assertEquals(1, locations.size(), "Error should only have one location.");
    assertEquals(new IndexRange(1, 6), locations.get(0).getIndexRange(), "Error should have the right location.");
  }

  static void testAutomaton(RepetitionTree repetition, boolean reluctant) {
    RegexTree x = repetition.getElement();
    FinalState finalState = assertType(FinalState.class, repetition.continuation());
    assertEquals(AutomatonState.TransitionType.EPSILON, repetition.incomingTransitionType());
    assertSingleEdge(repetition, x, AutomatonState.TransitionType.CHARACTER);
    BranchState branch = assertType(BranchState.class, x.continuation());
    assertSingleEdge(x, branch, AutomatonState.TransitionType.EPSILON);
    assertSame(finalState, branch.continuation());
    if (reluctant) {
      assertListElements(branch.successors(),
        assertEdge(finalState, AutomatonState.TransitionType.EPSILON),
        assertEdge(repetition, AutomatonState.TransitionType.EPSILON)
      );
    } else {
      assertListElements(branch.successors(),
        assertEdge(repetition, AutomatonState.TransitionType.EPSILON),
        assertEdge(finalState, AutomatonState.TransitionType.EPSILON)
      );
    }
  }
}
