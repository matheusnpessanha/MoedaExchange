package br.com.ConversorMoedas;

import br.com.ConversorMoedas.modelos.DadosMoedas;
import br.com.ConversorMoedas.modelos.DadosDaConversaoMoedas;

import java.io.*;
import java.time.Instant;
import java.util.InputMismatchException;
import java.util.Scanner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ConversorDeMoedas {
    protected String[][] moedas;
    protected String moedaDe;
    protected String moedaPara;
    protected String arquivo = "historico_conversao.txt";

    public ConversorDeMoedas(){
        ConsultarAPI api = new ConsultarAPI();
        String retorno;

        retorno = api.consultaRota("https://v6.exchangerate-api.com/v6/e29408fdf0fe053a5655c5a5/codes");

        FormataConsulta formata = new FormataConsulta();
        DadosMoedas moedas = formata.consultaToJson(retorno, DadosMoedas.class);

        this.moedas = moedas.supported_codes();
    }

    public void limparTela() {
        String sistemaOperacional = System.getProperty("os.name").toLowerCase();

        try {
            if (sistemaOperacional.contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Erro ao limpar o console: " + e.getMessage());
        }
    }

    public void menu(){
        int opcao = 1;
        Scanner leitura = new Scanner(System.in);

        while (opcao != 0) {
            try {
                telaMenu();
                opcao = leitura.nextInt();

                switch (opcao){
                    case 1:
                        limparTela();
                        exibeMoedas();
                        defineMoeda(true);//Origem
                        defineMoeda(false);//Destino
                        converterDePara();
                        break;
                    case 2:
                        limparTela();
                        exibeHistorico();
                        break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Erro: Entrada inválida. Informe um valor contido no menu.");
                leitura.nextLine(); // Limpa o buffer de entrada
            }
        }
    }

    private void telaMenu(){
        String menu = """
                   CONVERSOR DE MOEDAS
                """;
        System.out.println(menu);

        String legendaTraco = String.valueOf('-').repeat(90);
        System.out.println(legendaTraco);
        System.out.println("""
                                    
                                                1 - Nova Conversão
                                                2 - Histórico de Conversão
                                                0 - Encerrar
                """);
        System.out.println(legendaTraco);
        System.out.println("=> ");
    }

    public void defineMoeda(boolean tipo){
        String legendaTraco = String.valueOf('*').repeat(50);
        String tipoMoeda = tipo ? "Origem" : "Destino";

        int moeda = -1;

        while (!verificaEscolhaMoeda(moeda)) {
            boolean entradaValida = false;
            Scanner leitura = new Scanner(System.in);

            while (!entradaValida) {
                System.out.printf("     %s Informe o número da moeda para conversão [%s] %s \n => ", legendaTraco, tipoMoeda, legendaTraco);
                try {
                    moeda = leitura.nextInt();
                    entradaValida = true;
                } catch (InputMismatchException e) {
                    System.out.println("Erro: Entrada inválida. Digite um número inteiro que esteja na lista.");
                    leitura.nextLine(); // Limpa o buffer de entrada
                }
            }
        }

        if(tipo){//Origem
            this.moedaDe = moedas[moeda][0];
        }else{//Destino
            this.moedaPara = moedas[moeda][0];
        }
        System.out.printf("\n Moeda escolhida: %s - %s \n\n", moedas[moeda][0], moedas[moeda][1]);
    }

    public void exibeMoedas() {
        int moedasPorLinha = 3; // número de moedas por linha
        int count = 0;
        int larguraColunaIndice = 3; // Largura da coluna do índice
        int larguraColunaCodigo = 3; // Largura da coluna do código
        int larguraColunaNome = 40; // Largura da coluna do nome

        String legendaTraco = String.valueOf('-').repeat(70);
        System.out.printf("     %s Moedas Disponíveis %s ", legendaTraco, legendaTraco);
        System.out.println();

        for (String[] codigo : this.moedas) {
            System.out.printf("[ %-" + larguraColunaIndice + "s | %-" + larguraColunaCodigo + "s - %-" + larguraColunaNome + "s ] ", count, codigo[0], codigo[1]);
            count++;
            if (count % moedasPorLinha == 0) {
                System.out.println();
            }
        }
        System.out.println();
    }

    private boolean verificaEscolhaMoeda(int escolha) {
        return escolha > -1 && escolha < moedas.length;
    }

    public void converterDePara(){
        boolean entradaValida = false;
        double valor = 0;
        Scanner leitura = new Scanner(System.in);
        String legendaTraco = String.valueOf('*').repeat(50);

        //valida entrada do valor
        while (!entradaValida) {
            System.out.printf("     %s Informe o valor para conversão  %s \n => ", legendaTraco, legendaTraco);
            try {
                valor = leitura.nextInt();
                entradaValida = true;
            } catch (InputMismatchException e) {
                System.out.println("Erro: Entrada inválida. Informe um número.");
                leitura.nextLine(); // Limpa o buffer de entrada
            }
        }

        ConsultarAPI api = new ConsultarAPI();
        String retorno;

        retorno = api.consultaRota("https://v6.exchangerate-api.com/v6/e29408fdf0fe053a5655c5a5/pair/"+moedaDe+"/"+moedaPara);

        FormataConsulta formata = new FormataConsulta();
        DadosDaConversaoMoedas conversao = formata.consultaToJson(retorno, DadosDaConversaoMoedas.class);

        //Tratando dados
        String ultimaData = converteData(conversao.time_last_update_unix());
        String atualData = converteData(Instant.now().getEpochSecond());

        legendaTraco = String.valueOf('*').repeat(70);

        // Informações a serem salvas
        StringBuilder conteudo = new StringBuilder();
        conteudo.append("\n").append(legendaTraco).append("\n");
        conteudo.append(String.format("\nConversão: %s => %s%n", moedaDe, moedaPara));
        conteudo.append("\n\n");
        conteudo.append(String.format("Taxa de conversão: %s %.4f%n", moedaPara, conversao.conversion_rate()));
        conteudo.append(String.format("Valor Informado: %s %.2f%n", moedaDe, valor));
        conteudo.append(String.format("Valor Convertido: %s %.2f%n", moedaPara, conversao.conversion_rate() * valor));
        conteudo.append("\n");
        conteudo.append(String.format("Última atualização do valor de conversão: %s%n", ultimaData));
        conteudo.append(String.format("Data conversão: %s%n", atualData));
        conteudo.append("\n").append(legendaTraco).append("\n");

        System.out.println(conteudo);
        salvaHistorico(conteudo);
    }

    private String converteData(long data){
        LocalDateTime dataConvertida = LocalDateTime.ofInstant(Instant.ofEpochSecond(data), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return dataConvertida.format(formatter);
    }

    private void salvaHistorico(StringBuilder conteudo){
        try (FileWriter fileWriter = new FileWriter(arquivo, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(conteudo);
        } catch (IOException e) {
            System.out.println("Erro ao adicionar as informações no histórico: " + e.getMessage());
        }
    }

    public void exibeHistorico(){

        System.out.println("""
                                    HISTÓRICO DE CONVERSÕES
                """);

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                System.out.println(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo " + arquivo + ": " + e.getMessage());
        }

        System.out.println("\s                                            Fim Histórico\s ");
        System.out.println();
    }
}
