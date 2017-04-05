package org.combinators.solitaire.freecell

import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.{FieldDeclaration, MethodDeclaration, BodyDeclaration}
import com.github.javaparser.ast.expr.{Expression, NameExpr}
import com.github.javaparser.ast.stmt.Statement
import de.tu_dortmund.cs.ls14.cls.interpreter.combinator
import de.tu_dortmund.cs.ls14.cls.types._
import de.tu_dortmund.cs.ls14.cls.types.syntax._
import de.tu_dortmund.cs.ls14.twirl.Java
import org.combinators.solitaire.shared.GameTemplate
import org.combinators.solitaire.shared.Score52

// domain
import domain._
import domain.freeCell.HomePile
import domain.freeCell.FreePile

// this now becomes available
class GameDomain(val solitaire:Solitaire) extends GameTemplate with Score52 {


	// Be sure to avoid 'var' within the exterior of a combinator.
//	@combinator object NumHomePiles {
//		def apply: Expression = {
//			val f = solitaire.getFoundation()
//					Java("" + f.size()).expression()
//	}
//	val semanticType: Type = 'NumHomePiles
//	}

//	@combinator object NumFreePiles {
//		def apply: Expression = {
//			val r = solitaire.getReserve()
//					Java("" + r.size()).expression()
//	}
//	val semanticType: Type = 'NumFreePiles
//	}

//	@combinator object NumColumns {
//		def apply: Expression = {
//			val t = solitaire.getTableau()
//					Java("" + t.size()).expression()
//	}
//
//	val semanticType: Type = 'NumColumns
//	}

	// Mapping into Java is a concern for this scala trait.

	@combinator object RootPackage {
		def apply: NameExpr = {
			Java("org.combinators.solitaire.freecell").nameExpression
	}
	val semanticType: Type = 'RootPackage
	}

	@combinator object NameOfTheGame {
		def apply: NameExpr = {
			Java("FreeCell").nameExpression
	}
	val semanticType: Type = 'NameOfTheGame
	}

	// @(NameOfTheGame: String, NumColumns:Expression, NumHomePiles: Expression, NumFreePiles: Expression)


	//  @combinator object Initialization {
	//    def apply(nameOfTheGame: NameExpr, numColumns:Expression, numHomePiles:Expression, numFreePiles: Expression): Seq[Statement] = {
	//      
	//      java.Initialization.render(nameOfTheGame.toString(), numColumns, numHomePiles, numFreePiles).statements()
	//    }
	//    val semanticType: Type = 'NameOfTheGame =>: 'NumColumns =>: 'NumHomePiles =>: 'NumFreePiles =>: 'Initialization :&: 'NonEmptySeq
	//  }

	// FreeCell model derived from the domain model
	@combinator object FreeCellInitModel {

	// note: we could avoid passing in these parameters and just solely
	// visit the domain model. That is an alternative worth considering.

	def apply(): Seq[Statement] = {

	val NumFreePiles = solitaire.getReserve().size()
	val NumHomePiles = solitaire.getFoundation().size()
	val NumColumns   = solitaire.getTableau().size()

	Java(s"""
			  
	  // Basic start of pretty much any solitaire game that requires a deck.
          deck = new Deck ("deck");
          int seed = getSeed();
          deck.create(seed);
          addModelElement (deck);
            
         /* construct model elements */
	for (int i = 0; i < $NumColumns; i++) {
   	   fieldColumns[i] = new Column(ColumnsPrefix + (i+1));
	   addModelElement (fieldColumns[i]);		
	   fieldColumnViews[i] = new ColumnView(fieldColumns[i]);
	}

	for (int i = 0; i < $NumFreePiles; i++) {
	  fieldFreePiles[i] = new Pile (FreePilesPrefix + (i+1));
	  addModelElement (fieldFreePiles[i]);
	  fieldFreePileViews[i] = new PileView (fieldFreePiles[i]);
	}

	for (int i = 0; i < $NumHomePiles; i++) {
	  fieldHomePiles[i] = new Pile (HomePilesPrefix + (i+1));
	  addModelElement (fieldHomePiles[i]); 
	  fieldHomePileViews[i] = new PileView(fieldHomePiles[i]);
	}
	""").statements
		}

	val semanticType: Type = 'Init('Model)
}
	@combinator object FreeCellInitView {
		def apply(): Seq[Statement] = {

				//val found = Solitaire.getInstance().getFoundation()
		  val found = solitaire.getFoundation()
			val tableau = solitaire.getTableau()
			val NumColumns = tableau.size()
			val free = solitaire.getReserve()
			val lay = solitaire.getLayout()
			val rectFound = lay.get(Layout.Foundation)
			val rectFree = lay.get(Layout.Reserve)
			val rectTableau = lay.get(Layout.Tableau)

				// (a) do the computations natively in scala to generate java code
				// (b) delegation to Layout class, but then needs to pull back into
				//     scala anyway
				//
				// card is 73 x 97

				// HACK! How to create empty sequence to start loop with?
				var stmts = Java("System.out.println(\"Place Foundation and FreeCell Views\");").statements()

				// This could be a whole lot simpler! This places cards within Foundation rectangle
				// with card width of 97 cards each. Gap is fixed and determined by this function
				// Missing: Something that *maps* the domain model to 'fieldHomePileViews' and construction
				val it = lay.placements(Layout.Foundation, found, 97)
				var idx = 0
				while (it.hasNext()) {
				  val r = it.next()
				  
          val s = Java(s"""

									fieldHomePileViews[$idx].setBounds(${r.x}, ${r.y}, cw, ch);
									addViewWidget(fieldHomePileViews[$idx]);
									
									""").statements()

					idx = idx + 1
					stmts = Java(stmts.mkString("\n") + "\n" + s.mkString("\n")).statements()
				}
				
				// would be useful to have Scala utility for appending statements to single body.
				idx = 0
				while (idx < found.size()) {
					val xfree = rectFree.x + 15*idx + idx*73
					val s = Java(s"""

							fieldFreePileViews[$idx].setBounds($xfree, 20, cw, ch);
							addViewWidget(fieldFreePileViews[$idx]);

							""").statements()

					idx = idx + 1
					stmts = Java(stmts.mkString("\n") + "\n" + s.mkString("\n")).statements()
				}

				// now column placement
				idx = 0
				while (idx < tableau.size()) {
					val xtabl = rectTableau.x + 15*idx + idx*73
					val s = Java(s"""

							fieldColumnViews[$idx].setBounds($xtabl, 40 + ch, cw, 13*ch);
							addViewWidget(fieldColumnViews[$idx]);

							""").statements()

					idx = idx + 1
					stmts = Java(stmts.mkString("\n") + "\n" + s.mkString("\n")).statements()
				}
				
				stmts
		}
		
		val semanticType: Type = 'Init('View)
	}

	@combinator object FreeCellInitControl {
		def apply(NameOfGame:NameExpr): Seq[Statement] = {

		val nc = solitaire.getTableau().size()
		val np = solitaire.getFoundation().size()
		val nf = solitaire.getReserve().size()
		val name = NameOfGame.toString()

		Java(s"""
			// setup controllers
			for (int i = 0; i < $nc; i++) {
			fieldColumnViews[i].setMouseMotionAdapter (new SolitaireMouseMotionAdapter (this));
			fieldColumnViews[i].setUndoAdapter (new SolitaireUndoAdapter (this));
			fieldColumnViews[i].setMouseAdapter (new ${name}ColumnController (this, fieldColumnViews[i]));
			}
    
    						for (int i = 0; i < $np; i++) {
      						fieldHomePileViews[i].setMouseMotionAdapter (new SolitaireMouseMotionAdapter (this));
      						fieldHomePileViews[i].setUndoAdapter (new SolitaireUndoAdapter (this));
      						fieldHomePileViews[i].setMouseAdapter (new HomePileController (this, fieldHomePileViews[i]));
    						}
    
    						for (int i = 0; i < $nf; i++) {
      						fieldFreePileViews[i].setMouseMotionAdapter (new SolitaireMouseMotionAdapter (this));
      						fieldFreePileViews[i].setUndoAdapter (new SolitaireUndoAdapter (this));
      						fieldFreePileViews[i].setMouseAdapter (new FreeCellPileController (this, fieldFreePileViews[i]));
    						}

								""").statements()

		}

		val semanticType: Type = 'NameOfTheGame =>: 'Init('Control)
	}

	// generic deal cards from deck into the tableau
	@combinator object FreeCellInitLayout {
		def apply(): Seq[Statement] = {
      val tableau = solitaire.getTableau()
      val NumColumns = tableau.size()

      // HACK! How to create empty sequence to start loop with?
      var stmts = Java("System.out.println(\"Complete initial deal.\");").statements()

	// standard logic to deal to all tableau cards
	var numColumns = tableau.size() 
	val s = Java(s"""
		int col = 0;
		while (!deck.empty()) {
  		fieldColumns[col++].add(deck.get());
  		if (col >= $numColumns) {
  		  col = 0;
  		}
		}
		""").statements()
	stmts = Java(stmts.mkString("\n") + "\n" + s.mkString("\n")).statements()

	stmts
	}

	val semanticType: Type = 'Init('Layout)
     }

	// create three separate blocks based on the domain model.
	@combinator object Initialization {
		def apply(minit:Seq[Statement], vinit:Seq[Statement], cinit:Seq[Statement], layout:Seq[Statement]): Seq[Statement] = {

				// @(ModelInit: Seq[Statement], ViewInit: Seq[Statement], ControlInit : Seq[Statement], SetupInitialState : Seq[Statement])
				java.DomainInit.render(minit, vinit, cinit, layout).statements
		}
		val semanticType: Type = 'Init('Model) =>: 'Init('View) =>: 'Init('Control) =>: 'Init('Layout) =>: 'Initialization :&: 'NonEmptySeq
	}

	// vagaries of java imports means these must be defined as well.
	@combinator object ExtraImports {
		def apply(nameExpr:NameExpr): Seq[ImportDeclaration] = {
				Seq(Java("import " + nameExpr.toString() + ".controller.*;").importDeclaration(),
						Java("import " + nameExpr.toString() + ".model.*;").importDeclaration()
						)
		}
		val semanticType: Type = 'RootPackage =>: 'ExtraImports
	}

	@combinator object ExtraMethods {
		def apply(): Seq[MethodDeclaration] = {

		val reserve = solitaire.getReserve().size()
		val tableau = solitaire.getTableau().size()
		val numFreePiles= Java(s"$reserve").expression()
		val numColumns = Java(s"$tableau").expression()

		java.ExtraMethods.render(numFreePiles, numColumns).classBodyDeclarations().map(_.asInstanceOf[MethodDeclaration])
		}
		val semanticType: Type = 'ExtraMethods :&: 'Column('FreeCellColumn, 'AutoMovesAvailable) 
	}


	@combinator object EmptyExtraMethods {
		def apply(): Seq[MethodDeclaration] = Seq.empty
				val semanticType: Type = 'ExtraMethodsBad
	}


	// This maps the elements in the Solitaire domain model into actual java fields.
	@combinator object ExtraFields {
	  def apply(): Seq[FieldDeclaration] = {

		var fields = Java("IntegerView scoreView;\nIntegerView numLeftView;").classBodyDeclarations().map(_.asInstanceOf[FieldDeclaration])

		val found = solitaire.getFoundation()
		val reserve = solitaire.getReserve()
		val tableau = solitaire.getTableau()
		val stock = solitaire.getStock()
		
		// turn Stock into deck. FIX ME. HOW TO DO IF CONDITIONAL?
		val decks = Java("Deck deck;").classBodyDeclarations().map(_.asInstanceOf[FieldDeclaration])
		if (stock.getNumDecks() > 1) {
		  val multidecks = Java("MultiDeck deck;").classBodyDeclarations().map(_.asInstanceOf[FieldDeclaration])
		}
		
		val fieldFreePiles = java.FieldsTemplate
		  .render("FreePile", "Pile", "PileView", ""+reserve.size())
		  .classBodyDeclarations()
		  .map(_.asInstanceOf[FieldDeclaration])
		
		val fieldHomePiles = java.FieldsTemplate
		  .render("HomePile", "Pile", "PileView", ""+found.size())
		  .classBodyDeclarations()
		  .map(_.asInstanceOf[FieldDeclaration])
		
    val fieldColumns = java.FieldsTemplate
		  .render("Column", "Column", "ColumnView", ""+tableau.size())
		  .classBodyDeclarations()
		  .map(_.asInstanceOf[FieldDeclaration])
		  
		// no doubt, better way of doing this
		Java(decks.mkString("\n") + "\n" + fields.mkString("\n") + "\n" + fieldFreePiles.mkString("\n") + fieldHomePiles.mkString("\n") + fieldColumns.mkString("\n"))
		    .classBodyDeclarations()
		    .map(_.asInstanceOf[FieldDeclaration])
  		}
		
		val semanticType: Type = 'ExtraFields
	}
}
