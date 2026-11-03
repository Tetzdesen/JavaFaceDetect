# Origem das imagens de teste

- `rosto_unico.jpg` (baseado em `lena.jpg`) e `multiplos_rostos.jpg` (baseado em `messi5.jpg`) e `sem_rosto.jpg`
  (baseado em `fruits.jpg`) são imagens de amostra distribuídas com o próprio projeto OpenCV
  (`opencv/samples/data/`), sob a licença Apache 2.0 do OpenCV, usadas amplamente pela comunidade
  como material de teste/tutorial para detecção facial com Haar Cascade.
- `pessoa_a_*.png` e `pessoa_b_*.png` são recortes de rosto derivados das imagens acima (`lena.jpg` e
  `messi5.jpg`), convertidos para escala de cinza e levemente transformados (rotação/brilho/ruído), usados
  apenas para testar o treinamento e a comparação do modelo LBPH com duas identidades distintas.

Essas imagens são usadas exclusivamente como fixtures de teste automatizado deste módulo.
