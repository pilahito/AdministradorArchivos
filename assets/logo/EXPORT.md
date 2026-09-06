# Exportar logo CloudTerm Pro

Fuente: `cloudterm-pro.svg` (nube + prompt `>_` + escudo).

## Tamaños PNG recomendados

| Uso | Tamaño |
|-----|--------|
| Launcher mdpi | 48×48 |
| Launcher hdpi | 72×72 |
| Launcher xhdpi | 96×96 |
| Launcher xxhdpi | 144×144 |
| Launcher xxxhdpi | 192×192 |
| Play Store | 512×512 |
| GitHub social | 1280×640 (con padding) |
| Favicon | 32×32, 16×16 |

## Herramientas

```bash
# con Inkscape
inkscape cloudterm-pro.svg -o cloudterm-pro-512.png -w 512 -h 512

# con rsvg-convert
rsvg-convert -w 512 -h 512 cloudterm-pro.svg > cloudterm-pro-512.png
```

Colores marca: fondo `#0F172A`, cian `#00F0FF`, violeta `#7C6CF0`, esmeralda `#00E676`.