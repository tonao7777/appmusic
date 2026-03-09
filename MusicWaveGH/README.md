# 🎵 MusicWave — Build via GitHub Actions

## Como gerar o APK sem instalar nada

### Passo a Passo (leva ~5 minutos)

---

### 1. Criar conta no GitHub (se não tiver)
Acesse https://github.com e crie uma conta gratuita.

---

### 2. Criar um repositório novo
1. Clique no botão **"+"** no canto superior direito → **"New repository"**
2. Nome: `musicwave` (ou qualquer nome)
3. Deixe como **Public** ou **Private** (ambos funcionam)
4. Clique em **"Create repository"**

---

### 3. Subir os arquivos
**Opção A — pelo site (mais fácil):**
1. Na página do repositório, clique em **"uploading an existing file"**
2. Arraste TODOS os arquivos desta pasta para o site
3. **IMPORTANTE:** Mantenha a estrutura de pastas! Suba pasta por pasta.
4. Clique em **"Commit changes"**

**Opção B — pelo Git (mais rápido se souber usar):**
```bash
cd MusicWave
git init
git remote add origin https://github.com/SEU_USUARIO/musicwave.git
git add .
git commit -m "primeiro commit"
git push -u origin main
```

---

### 4. O GitHub vai compilar automaticamente!
Após subir os arquivos, vá em:
**Aba "Actions"** → clique no workflow em execução → aguarde ~3-5 minutos

---

### 5. Baixar o APK
1. Quando o workflow ficar **verde** (✅), clique nele
2. Role para baixo até **"Artifacts"**
3. Clique em **"MusicWave-APK"** para baixar
4. Extraia o ZIP e instale o `app-debug.apk` no celular

> **Para instalar no celular:** Ative "Fontes desconhecidas" em:
> Configurações → Segurança → Instalar apps desconhecidos

---

## Funcionalidades do App
- 🔍 **Escaneia TODAS as músicas** do dispositivo automaticamente
- ▶️ **Autoplay** ao terminar cada faixa
- 🔀 Modo aleatório | 🔁 Repetição
- 🎛️ **Notificação** com nome da música tocando
- 🔍 Busca por título, artista ou álbum
- 🎨 Interface escura elegante com animações
