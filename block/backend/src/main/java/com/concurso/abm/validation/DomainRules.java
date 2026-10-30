package com.concurso.abm.validation;

import com.concurso.abm.entity.*;
import com.concurso.abm.exception.DomainException;
import java.util.*;

public final class DomainRules {
  private DomainRules() {}

  public static String text(String value) {
    return value == null ? null : value.strip();
  }

  public static void unique(boolean duplicate) {
    if (duplicate)
      throw DomainException.conflict(
          "Ya existe un registro activo con ese nombre en esta ubicación");
  }

  public static void unused(boolean used) {
    if (used)
      throw DomainException.conflict("No se puede eliminar: tiene registros activos asociados");
  }

  public static boolean same(String a, String b) {
    return a != null && b != null && a.equalsIgnoreCase(b);
  }
}
