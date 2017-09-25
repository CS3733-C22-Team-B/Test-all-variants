package domain.moves;

import domain.*;
import java.util.*;

/**
 * Calls for the removal of a number of cards, all from the source container.
 * 
 * All elements from the container would suffer the removal of a card.
 * These move classes might not be necessary.
 */
public class RemoveMultipleCardsMove extends Move {

    /** 
     * Determine conditions for removing multiple cards from container 
     */
    public RemoveMultipleCardsMove (String name, Container src, ConstraintStmt constraint) {
	super(name, src, constraint);
    }

    /** Extract constraint associated with move. */
    public ConstraintStmt getConstraint() { return constraint; }

    public String toString() {
        return super.toString() + " : " + constraint;
    }

   /** Get the source element of this move type. */
   public Element   getSource() {
      Iterator<Element> it = getSourceContainer().iterator();
      if (it == null || !it.hasNext()) { return null; }
      return it.next();
   }

   /** Get the target element of this move type. */
   public Element   getTarget() {
      Optional<Container> opt = getTargetContainer();
      if (!opt.isPresent()) { return null; }
      
      Iterator<Element> it = opt.get().iterator();
      if (it == null || !it.hasNext()) { return null; }
      return it.next();
   }

   /** Get element being moved. Hack to make work for FreeCell. */
   public Element   getMovableElement() {
     return new Card(Rank.ACE, Suit.SPADES); 
   }
}