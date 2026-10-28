# Módulo 4: Git y GitHub

## Estrategia

Usaría `git cherry-pick` para aplicar en `feature/new-login` el commit que corrige el problema de seguridad. Crea un nuevo commit con ese cambio, sin incorporar el resto de las actualizaciones de `main`.

## Comandos

```bash
git status
git fetch origin
git switch feature/new-login
git log --oneline origin/main
```

- `status`: comprobar el estado del trabajo local.
- `fetch origin`: actualizar las referencias remotas sin fusionar cambios en mi rama.
- `switch`: situarme en la rama que necesita la corrección.
- `log`: identificar el hash del commit de seguridad.

En este ejemplo, `abc1234` representa el hash real de la corrección:

```bash
git cherry-pick -x abc1234
```

`-x` agrega al mensaje una referencia al commit de origen para facilitar su trazabilidad.

No usaría `merge origin/main` ni un rebase sobre toda `main`, porque incorporarían actualizaciones ajenas al cambio solicitado.

si salen conflictos resolvería manualmente los archivos afectados y continuaría

## Verificación y publicación

Ejecutaría las pruebas del proyecto y verificaría especialmente el escenario de seguridad corregido. Después subiría la rama:

```bash
git push origin feature/new-login
```
