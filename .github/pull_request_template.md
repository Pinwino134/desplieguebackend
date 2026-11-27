## Solicitud de Cambio (RFC / Pull Request)

### Tipo de cambio
- [ ] Nueva funcionalidad (Sprint / Feature)
- [ ] Corrección de defecto (Bugfix)
- [ ] Seguridad / Hardening
- [ ] Rendimiento / Capacidad / Infraestructura
- [ ] Documentación

### Descripción del cambio
<!-- Explica qué se modificó y qué proceso de negocio o requerimiento atiende -->

### Evaluación de impacto y plan de reversión (Rollback)
- **Servicios afectados:** (ej. `apigateway`, `serviciosservice`, `postgres`)
- **¿Requiere migración de BD o nuevas variables de entorno?** Sí / No
- **Plan de reversión:** `git revert <commit>` y redespliegue vía GitHub Actions.

### Checklist de Control de Cambios (CI)
- [ ] Pruebas unitarias y de seguridad ejecutadas sin errores (`mvn test`).
- [ ] Sin credenciales ni secretos hardcodeados en el código.
- [ ] Rutas y permisos RBAC verificados en `apigateway.yml`.
- [ ] `CHANGELOG.md` actualizado.
