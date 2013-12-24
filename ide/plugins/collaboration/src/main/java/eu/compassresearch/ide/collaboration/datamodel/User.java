package eu.compassresearch.ide.collaboration.datamodel;

public class User extends Model {
	
	private static final long serialVersionUID = 5634470790158709661L;
	
	private String postfix;
	
	public User(String name) {
		super(name);
	}
	
	public User(String name, String namePostfix)
	{
		super(name);
		postfix = namePostfix;
	}

	public void accept(IModelVisitor visitor, Object passAlongArgument) {
		
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return (postfix != null && !postfix.isEmpty() ? name + " " + postfix : name );
	}

	public String getPostfix()
	{
		return postfix;
	}

	public void setPostfix(String postfix)
	{
		this.postfix = postfix;
		fireObjectAddedEvent(this);
	}
}
