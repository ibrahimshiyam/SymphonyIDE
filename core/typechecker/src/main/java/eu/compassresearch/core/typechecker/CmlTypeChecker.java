package eu.compassresearch.core.typechecker;

import java.lang.reflect.Method;
import java.util.List;

import eu.compassresearch.ast.lex.LexLocation;
import eu.compassresearch.ast.node.INode;

/**
 * 
 * @author rwl
 * 
 *         A CML Type checker has the characteristics of this interface.
 * 
 */
public interface CmlTypeChecker
  {
    // /----------------------------------------------------------
    // | Error reporting
    // \----------------------------------------------------------
    /**
     * @author rwl
     * 
     *         The result from the CML type checker is a set of CMLIssues. These
     *         can be warnings or error.
     * 
     *         A CML Issue points the a subtree node that is the least upper
     *         bound node in the AST spanning causing the issue.
     * 
     */
    public static abstract class CMLIssue
      {
        protected final INode subtree;
        
        public CMLIssue(INode subtree)
          {
            this.subtree = subtree;
          }
        
        // temporary method goes away when astCreator is updated. ( INode should
        // have getLocation method )
        public LexLocation getLocation()
          {
            LexLocation location = null;
            if (subtree != null)
              {
                try
                  {
                    Method getLocation = subtree.getClass().getMethod(
                        "getLocation", new Class<?>[0]);
                    location = (LexLocation) getLocation.invoke(subtree,
                        new Object[0]);
                  } catch (Exception e)
                  {
                    // no location :(
                  }
              }
            return location;
          }
      }
    
    /**
     * 
     * @author rwl
     * 
     *         CML Type Warnings can be ignored as the properties of CML special
     *         preservation and CML special progress should still hold in the
     *         presents of warnings. However, the model may be ill shaped for
     *         e.g. simulation: a set of classes with no processes can never
     *         run. Such issues are not directly errors but will lead to limited
     *         exploration with later phases of the COMPASS tools.
     */
    public static class CMLTypeWarning extends CMLIssue
      {
        protected final String description;
        
        public String getDescription()
          {
            return description;
          }
        
        public CMLTypeWarning(INode subtree, String description)
          {
            super(subtree);
            this.description = description;
          }
        
        @Override
        public String toString()
          {
            LexLocation location = super.getLocation();
            return "TypeWarning: " + location + " : " + description;
          }
        
      }
    
    /**
     * 
     * @author rwl
     * 
     *         CML Type Errors means that the CML model leads to an AST that
     *         cannot be given a proper semantics.
     * 
     */
    public static class CMLTypeError extends CMLTypeWarning
      {
        
        public CMLTypeError(INode subtree, String message)
          {
            super(subtree, message);
          }
        
        @Override
        public String toString()
          {
            LexLocation location = super.getLocation();
            return "TypeError: " + location + " : " + description;
          }
      }
    
    /**
     * Returns the list of type errors found after type checking. If the type
     * check method has not been invoked yet an IllegalStateException will be
     * thrown.
     * 
     * @return - List with CML type errors.
     * @throws IllegalStateException
     */
    public List<CMLTypeError> getTypeErrors() throws IllegalStateException;
    
    /**
     * Returns the list of type warnings found during type checking. See
     * {@link CMLTypeWarning}. An IllegalStateException will be thrown is
     * invoked before the type checker has completed a full type check.
     * 
     * @return
     * @throws IllegalStateException
     */
    public List<CMLTypeWarning> getTypeWarnings() throws IllegalStateException;
    
    /**
     * Run the type checker. This will update the source(s) this type checker
     * instance was constructed with.
     * 
     * @return - Returns true if the entire tree could be type checked without
     *         errors. It returns false otherwise and courses to failing the
     *         type check can be inspected though getErrors.
     */
    public boolean typeCheck();
    
    /**
     * Return a pretty name for this type checker user-interactive tools can use
     * for printing.
     * 
     * @return - analysis pretty name.
     */
    public String getAnalysisName();
  }
