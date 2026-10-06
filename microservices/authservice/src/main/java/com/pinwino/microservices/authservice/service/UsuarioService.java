package com.pinwino.microservices.authservice.service;

import com.pinwino.microservices.authservice.model.TipoActor;
import com.pinwino.microservices.authservice.model.Usuario;

import java.util.List;

public interface UsuarioService {
    List<Usuario> listarTodos();
    List<Usuario> listarPorTipo(TipoActor tipoActor);
    Usuario buscarPorId(Long id);
    Usuario buscarPorEmail(String email);
    Usuario crear(Usuario usuario);
    Usuario actualizar(Long id, Usuario usuario);
    void eliminar(Long id);
    Usuario autenticar(String email, String password);
}
