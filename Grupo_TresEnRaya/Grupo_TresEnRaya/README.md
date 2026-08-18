# Tres en raya con JavaFX

Proyecto Maven compatible con NetBeans que implementa un juego contra la computadora.

La aplicación incluye una pantalla de entrada con nombre de jugador, una interfaz de temática espacial felina,
fondos que cambian en cada partida y una explicación opcional de las decisiones de minimax.

Los usuarios se registran con correo, contraseña y nombre. Sus resultados se guardan localmente en
`data/users.dat` mediante serialización.

## Requisitos

- JDK 21 o posterior.
- NetBeans con soporte para Maven.

## Ejecución en NetBeans

1. Abrir NetBeans.
2. Seleccionar `File > Open Project`.
3. Elegir la carpeta `Grupo_TresEnRaya`.
4. Esperar a que Maven descargue JavaFX.
5. Ejecutar el proyecto con `Run Project`.

También puede ejecutarse desde una terminal con `mvn clean javafx:run`.

## Organización

- `model`: tablero, fichas, movimientos y sesión de juego.
- `model/UserRepository`: carga y guarda usuarios en un archivo local.
- `structures`: implementación encapsulada del árbol n-ario.
- `ai`: utilidad, generación del árbol y decisión minimax.
- `ui`: interfaz gráfica y eventos de JavaFX.
- `resources/images`: colección de fondos utilizada por la interfaz.

El árbol mantiene su raíz privada. La inteligencia agrega estados mediante las operaciones públicas del TDA y no accede a nodos internos.
