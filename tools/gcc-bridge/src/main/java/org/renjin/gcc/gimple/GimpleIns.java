package org.renjin.gcc.gimple;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "type")  
	@JsonSubTypes({  
	    @Type(value = GimpleAssign.class, name = "assign"),  
	    @Type(value = GimpleCall.class, name = "call"),
	    @Type(value = GimpleConditional.class, name = "conditional" ),
	    @Type(value = GimpleReturn.class, name = "return"),
	    @Type(value = GimpleGoto.class, name = "goto"),
	    @Type(value = GimpleSwitch.class, name = "switch" )}) 
public abstract class GimpleIns {

  public abstract void visit(GimpleVisitor visitor);

}
