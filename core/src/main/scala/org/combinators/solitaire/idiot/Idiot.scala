package org.combinators.solitaire.idiot

import javax.inject.Inject

import com.github.javaparser.ast.CompilationUnit
import de.tu_dortmund.cs.ls14.cls.interpreter.ReflectedRepository
import de.tu_dortmund.cs.ls14.cls.types.syntax._
import de.tu_dortmund.cs.ls14.git.InhabitationController
import domain.idiot.Domain
import org.webjars.play.WebJarsUtil
// domain
import domain._


class Idiot @Inject()(webJars: WebJarsUtil) extends InhabitationController(webJars) {


  val s:Solitaire = new Domain()

  // also register special

  // FreeCellDomain is base class for the solitaire variation. Note that this
  // class is used (essentially) as a placeholder for the solitaire val,
  // which can then be referred to anywhere as needed.
  lazy val repository = new gameDomain(s) with controllers {}
  lazy val Gamma:ReflectedRepository[gameDomain] = repository.init(ReflectedRepository(repository, classLoader = this.getClass.getClassLoader), s)

  lazy val combinatorComponents = Gamma.combinatorComponents
  lazy val jobs =
    Gamma.InhabitationBatchJob[CompilationUnit]('SolitaireVariation :&: 'Solvable)
    ///  .addJob[CompilationUnit]('EnhancedSolitaire)
      .addJob[CompilationUnit]('Controller('Deck))
      .addJob[CompilationUnit]('Controller('Column))
      .addJob[CompilationUnit]('Move('RemoveCard :&: 'GenericMove , 'CompleteMove))
      .addJob[CompilationUnit]('Move('MoveCard :&: 'GenericMove,   'CompleteMove))
      .addJob[CompilationUnit]('Move('MoveCard :&: 'PotentialMove, 'CompleteMove))
      .addJob[CompilationUnit]('Move('DealDeck :&: 'GenericMove,   'CompleteMove))

  lazy val results:Results = Results.addAll(jobs.run())

}
