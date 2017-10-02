package org.combinators.solitaire.freecell

// name clash
import com.github.javaparser.ast.`type`.{Type => JType}

import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.stmt.Statement
import de.tu_dortmund.cs.ls14.cls.types.Type
import de.tu_dortmund.cs.ls14.cls.types.syntax._
import de.tu_dortmund.cs.ls14.twirl.Java
import org.combinators.solitaire.shared._
import org.combinators.solitaire.shared
import de.tu_dortmund.cs.ls14.cls.interpreter.ReflectedRepository
import de.tu_dortmund.cs.ls14.cls.types.Constructor
import org.combinators.generic
import domain._
import domain.ui._


trait pilecontroller extends shared.Controller with shared.Moves with generic.JavaIdioms  {

  // dynamic combinators added as needed
  override def init[G <: SolitaireDomain](gamma : ReflectedRepository[G], s:Solitaire) :
  ReflectedRepository[G] = {
    var updated = super.init(gamma, s)
    println (">>> PileController dynamic combinators.")


    // not much to do, if no rules...
    if (s.getRules == null) {
      return updated
    }

    updated = createMoveClasses(updated, s)

    updated = createDragLogic(updated, s)

    updated = generateMoveLogic(updated, s)

    // for the PRESS while the TARGET is the locus for the RELEASE.
    // These are handling the PRESS events... SHOULD BE ABLE TO
    // INFER THESE FROM THE AVAILABLE MOVES
    updated = updated
      .addCombinator (new IgnorePressedHandler('HomePile, 'HomePile))
      .addCombinator (new IgnoreClickedHandler('HomePile, 'HomePile))
      .addCombinator (new SingleCardMoveHandler('FreePile, 'FreePile)) // ONLY PILE PRESS
      .addCombinator (new IgnoreClickedHandler('FreePile, 'FreePile))
      .addCombinator (new IgnoreClickedHandler('Column, 'Column))

    // Potential moves clarify structure (by type not instance). FIX ME
    // FIX ME FIX ME FIX ME
    updated = updated
      .addCombinator (new PotentialTypeConstructGen("Column", 'PlaceColumn))
      .addCombinator (new PotentialTypeConstructGen("Column", 'MoveColumn))
      .addCombinator (new PotentialTypeConstructGen("Column", 'BuildColumn))

    // these identify the controller names. SHOULD INFER FROM DOMAIN MODEL. FIX ME
    updated = updated
      .addCombinator (new ControllerNaming('FreePile, 'FreePile, "FreeCell"))
      .addCombinator (new ControllerNaming('HomePile, 'HomePile, "Home"))
      .addCombinator (new ControllerNaming('Column, 'Column, "FreeCell"))

    // Go through and assign GUI interactions for each of the known moves. Clean these up...
    val ui = new UserInterface(s)
    val els_it = ui.controllers
    while (els_it.hasNext) {
      val el = els_it.next()

      // generic widget controller with auto moves available since we
      // have that provided by our variation (see extra methods)
      print ("   ** " + el + ":WidgetController")
      if (s.hasAutoMoves) {
        updated = updated.addCombinator(new WidgetControllerWithAutoMoves(Symbol(el)))
      } else {
        updated = updated.addCombinator(new WidgetController(Symbol(el)))
      }
    }

    // CASE STUDY: Add Automove logic at end of release handlers
    // this is done by manipulating the chosen combinator.
    updated
  }

  /**
    * When a single card is being removed from the top card of a pile.
    */
  class SingleCardMoveHandler(source:Symbol, typ:Symbol) {
    def apply(): (SimpleName, SimpleName) => Seq[Statement] = {
      (widgetVariableName: SimpleName, ignoreWidgetVariableName: SimpleName) =>
        Java(s"""|$ignoreWidgetVariableName = false;
                 |Pile srcPile = (Pile) src.getModelElement();
                 |
                 |// Return in the case that the pile clicked on is empty
                 |if (srcPile.count() == 0) {
                 |  return;
                 |}
                 |$widgetVariableName = src.getCardViewForTopCard(me);
                 |if ($widgetVariableName == null) {
                 |  return;
                 |}""".stripMargin).statements()
    }

    val semanticType: Type =
      'Pair ('WidgetVariableName, 'IgnoreWidgetVariableName) =>:
        typ (source, 'Pressed) :&: 'NonEmptySeq
  }


  // Note: while I can have code within the apply() method, the semanticType
  // is static, so that must be passed in as is. These clarify that a
  // potential moveOneCardFromStack is still a Column Type.
//  class PotentialTypeConstructGen(constructor:Constructor) {
//    def apply(): JType = Java("Column").tpe()
//    val semanticType: Type = 'Move (constructor, 'TypeConstruct)
//  }


}


