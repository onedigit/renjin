package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.IntegerType;
import org.renjin.gcc.gimple.type.RealType;
import org.renjin.gcc.translate.expr.Expr;

public class TypeChecker {

  public static void assertMatch(Expr a, Expr b) {
    if(!a.type().equals(b.type())) {
      throw new IllegalArgumentException(String.format("Types do not match: %s:%s <> %s:%s",
          a.toString(), a.type().toString(),
          b.toString(), b.type().toString()));
    }
  }
  
  public static boolean isDouble(GimpleType type) {
    return type instanceof RealType && ((RealType) type).getPrecision() == 64;
  }
  
  public static boolean isInt(GimpleType type) {
    return type instanceof IntegerType && ((IntegerType) type).getPrecision() == 32;
  }

  public static boolean isLong(GimpleType type) {
    return type instanceof IntegerType && ((IntegerType) type).getPrecision() == 64;
  }
  
  public static String primitiveJvmTypeName(GimpleType type) {
    if(isDouble(type)) {
      return "double";
    } else if(isInt(type)) {
      return "int";
    } else if(isLong(type)) {
      return "long";
    } else {
      throw new UnsupportedOperationException();
    }
  }
}
