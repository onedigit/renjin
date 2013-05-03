package org.renjin.gcc.gimple.type;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "type")  
	@JsonSubTypes({  
	    @Type(value = IntegerType.class, name = "integer_type"),  
	    @Type(value = RealType.class, name = "real_type"),
	    @Type(value = PointerType.class, name = "pointer_type" ),
	    @Type(value = ReferenceType.class, name = "reference_type"),
	    @Type(value = ArrayType.class, name = "array_type"),
	    @Type(value = BooleanType.class, name = "boolean_type" )}) 
public interface GimpleType {

}
