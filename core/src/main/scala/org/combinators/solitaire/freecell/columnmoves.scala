package org.combinators.solitaire.freecell

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.`type`.{Type => JType}
import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.expr.{Name, SimpleName}
import com.github.javaparser.ast.stmt.Statement
import de.tu_dortmund.cs.ls14.cls.interpreter.combinator
import de.tu_dortmund.cs.ls14.cls.types.Type
import de.tu_dortmund.cs.ls14.cls.types.syntax._
import de.tu_dortmund.cs.ls14.twirl.Java
import domain._
import domain.constraints._
import org.combinators.solitaire.shared
import org.combinators.solitaire.shared._
import de.tu_dortmund.cs.ls14.cls.interpreter.ReflectedRepository
import de.tu_dortmund.cs.ls14.cls.types.Constructor


trait ColumnMoves extends shared.Moves {

 // dynamic combinators added as needed
  override def init[G <: SolitaireDomain](gamma : ReflectedRepository[G], s:Solitaire) :
      ReflectedRepository[G] = {
      var updated = super.init(gamma, s)
      println (">>> ColumnMoves dynamic combinators.")

 // Column to Column. These values are used by the conditionals. 
      val truth = new ReturnConstraint (new ReturnTrueExpression)
      val falsehood = new ReturnConstraint (new ReturnFalseExpression)
      val isEmpty = new ElementEmpty ("destination")

      val descend = new Descending("movingColumn")
      val alternating = new AlternatingColors("movingColumn")
      val cc_and = new AndConstraint (descend, alternating)

     // need constraint for sufficient space. Must write custom 
     // ConstraintGen that uses special method. However, this
     // would require modifying the generic constraints logic
     // which has a 'match' structure .
     val sufficientFreeToEmpty = 
         new ExpressionConstraint("((org.combinators.solitaire.freecell.FreeCell)game).numberVacant() - 1", ">=", "movingColumn.count()")

     val sufficientFree = 
         new ExpressionConstraint("((org.combinators.solitaire.freecell.FreeCell)game).numberVacant()", ">=", "movingColumn.count() - 1")

      val if4_inner =
        new IfConstraint(new OppositeColor("movingColumn.peek(0)", "destination.peek()"),
          new IfConstraint(new NextRank("destination.peek()", "movingColumn.peek(0)"),
            new IfConstraint(sufficientFree),
            falsehood),
          falsehood)

     val if4 =
        new IfConstraint(descend,
          new IfConstraint(alternating,
            new IfConstraint(isEmpty,
              new IfConstraint(sufficientFreeToEmpty),
              if4_inner),
            falsehood),
          falsehood)

      updated = updated
          .addCombinator (new StatementCombinator(if4,
                         'Move ('ColumnToColumn, 'CheckValidStatements)))

      updated
    }

}
