# UniversalScriptExecutor (V1)

Projeto Android em Kotlin para executar **scripts Lua dentro do próprio APK** e mostrar um overlay flutuante.

## O que esta V1 faz

- editor de Lua dentro do app;
- botão Executar / Parar;
- console de logs;
- `toast("texto")`;
- `log("texto")`;
- `sleep(ms)`;
- `should_stop()`;
- `overlay_text("texto")`;
- bolha flutuante `EX` usando a permissão "Exibir sobre outros apps";
- GitHub Actions para gerar um `app-debug.apk` automaticamente.

## O que esta V1 NÃO faz

Ela não injeta código, não lê/escreve memória de outros processos e não burla o sandbox do Android. O Lua roda dentro deste próprio aplicativo.

## Como gerar o APK no GitHub

1. Crie um repositório vazio.
2. Envie todo o conteúdo desta pasta para o repositório.
3. Abra **Actions** no GitHub.
4. Execute o workflow **Build APK** (ou faça um push na branch `main`/`master`).
5. Abra a execução concluída.
6. Em **Artifacts**, baixe `ScriptExecutor-debug`.
7. Dentro do ZIP estará `app-debug.apk`.

## Script de exemplo

```lua
log("Lua funcionando")
toast("Executor iniciado")
overlay_text("Script ativo")

for i = 1, 5 do
    if should_stop() then break end
    log("Passo " .. i)
    overlay_text("Passo " .. i .. "/5")
    sleep(1000)
end

overlay_text("Concluído")
```

## Próximos passos sugeridos

- biblioteca de scripts salvos;
- importar/exportar `.lua`;
- abas no editor;
- menu flutuante configurável via API Lua;
- ponte explícita com um app de teste que você controla.
