# Origem das imagens de teste

- `rosto_unico.jpg` (baseado em `lena.jpg`) e `multiplos_rostos.jpg` (baseado em `messi5.jpg`) e `sem_rosto.jpg`
  (baseado em `fruits.jpg`) são imagens de amostra distribuídas com o próprio projeto OpenCV
  (`opencv/samples/data/`), sob a licença Apache 2.0 do OpenCV, usadas amplamente pela comunidade
  como material de teste/tutorial para detecção facial com Haar Cascade.
- `pessoa_a_*.png` são recortes de rosto derivados de `lena.jpg`, convertidos para escala de cinza e
  levemente transformados (rotação/brilho/ruído).
- `pessoa_b_*.png` são recortes de rosto derivados de `astronaut.png` do scikit-image
  (`skimage/data/astronaut.png`, foto da astronauta Eileen Collins obtida do NASA Great Images database,
  em domínio público), convertidos para escala de cinza; a imagem de consulta tem enquadramento, rotação,
  brilho e ruído levemente diferentes da de cadastro.
- As imagens `pessoa_a_*` e `pessoa_b_*` mantêm o rosto inteiro com margem, de modo que passam pela
  detecção Haar Cascade e podem ser usadas tanto nos testes do modelo LBPH quanto nos testes da API.

Essas imagens são usadas exclusivamente como fixtures de teste automatizado deste módulo.
