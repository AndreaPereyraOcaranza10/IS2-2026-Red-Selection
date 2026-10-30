package com.concurso.abm.patterns.strategy;

import com.concurso.abm.entity.Usuario;

public interface IdentityStrategy {
  Usuario authenticate(String identity, String proof);
}
