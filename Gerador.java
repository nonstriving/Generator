package LACompiler;

public class Gerador extends LABaseVisitor<String>{
	
	private final SaidaParser sp;
	private PilhaDeTabelas pilhaTabela;
	private String tipoMaisVar;	

	public Gerador(SaidaParser sp){
		this.sp=sp;
	}

	public String getTipo(String tipo){
            switch (tipo) {
                case "inteiro":
                    return "%d";
                case "real":
                    return "%f";
                case "literal":
                case "char":
                    return "%s";
                default:
                    break;
            }
		return "";
	}

	@Override
	public String visitPrograma(LAParser.ProgramaContext ctx){
		if(ctx.children!=null){
			pilhaTabela=new PilhaDeTabelas();
			pilhaTabela.empilhar(new TabelaDeSimbolos("global"));
			sp.println("#include <stdio.h>\n#include <stdlib.h>\n");
			sp.println(visitDeclaracoes(ctx.declaracoes()));

			sp.println("int main(){");
			sp.println(visitCorpo(ctx.corpo()));

			sp.println("\treturn 0;\n}");
			pilhaTabela.desempilhar();
		}
		return "";
	}

	@Override
	public String visitDeclaracoes(LAParser.DeclaracoesContext ctx){
		if(ctx.children!=null){
			String declaracoes = "";
			declaracoes=declaracoes+visitDecl_local_global(ctx.decl_local_global());
			declaracoes=declaracoes+visitDeclaracoes(ctx.declaracoes());
			return declaracoes;
		}
		return "";
	}

	@Override
	public String visitDecl_local_global(LAParser.Decl_local_globalContext ctx){
		if(ctx.declaracao_local()!=null)
			return visitDeclaracao_local(ctx.declaracao_local());
		else if(ctx.declaracao_global()!=null)
			return visitDeclaracao_global(ctx.declaracao_global());
		return "";
	}

	@Override
	public String visitDeclaracao_local(LAParser.Declaracao_localContext ctx){
		String declaracao_local="";
		if(ctx.getText().startsWith("declare")){
			declaracao_local=declaracao_local+visitVariavel(ctx.variavel());
			return declaracao_local;
		}
		else if(ctx.getText().startsWith("constante")){
			pilhaTabela.topo().adicionarSimbolo(ctx.IDENT().toString(),ctx.tipo_basico().getText());
			declaracao_local=declaracao_local+"#define ";
			declaracao_local=declaracao_local+ctx.IDENT().toString()+" ";
			declaracao_local=declaracao_local+visitValor_constante(ctx.valor_constante());
			return declaracao_local;
		}
		else if(ctx.getText().startsWith("tipo")){
			pilhaTabela.topo().adicionarSimbolo(ctx.IDENT().getText(), "tipo");
			declaracao_local=declaracao_local+"typedef ";
			declaracao_local=declaracao_local+visitTipo(ctx.tipo());
			//O que eh isso?
			declaracao_local=declaracao_local+"treg;\n";
			return declaracao_local;
		}
		return "";
	}

	@Override
	public String visitVariavel(LAParser.VariavelContext ctx){
		if(ctx.children!=null){
			String variavel="";
			variavel=variavel+visitTipo(ctx.tipo())+" ";
			tipoMaisVar=ctx.tipo().getText();
			pilhaTabela.topo().adicionarSimbolo(ctx.IDENT().toString(),ctx.tipo().getText());
			variavel=variavel+ctx.IDENT().toString();
			variavel=variavel+visitDimensao(ctx.dimensao());
			if(tipoMaisVar.equals("literal")){
				variavel=variavel+"[80]";
			}
			variavel=variavel+visitMais_var(ctx.mais_var())+";\n";
			return variavel;
		}
		return "";
	}

	@Override
	public String visitMais_var(LAParser.Mais_varContext ctx){
		if(ctx.getText().startsWith(",")){
			String mais_var="";
			mais_var=mais_var+","+ctx.IDENT().toString();
			pilhaTabela.topo().adicionarSimbolo(ctx.IDENT().toString(), tipoMaisVar);
			mais_var=mais_var+visitDimensao(ctx.dimensao());
			mais_var=mais_var+visitMais_var(ctx.mais_var());
				return mais_var;
		}
		return "";
	}

	@Override
	public String visitIdentificador(LAParser.IdentificadorContext ctx){
		if(ctx.children!=null){
			String identificador="";
			identificador=identificador+visitPonteiros_opcionais(ctx.ponteiros_opcionais());
			identificador=identificador+ctx.IDENT().toString();
			identificador=identificador+visitDimensao(ctx.dimensao());
			identificador=identificador+visitOutros_ident(ctx.outros_ident());
			return identificador;
		}
		return "";
	}

	@Override
	public String visitPonteiros_opcionais(LAParser.Ponteiros_opcionaisContext ctx) {
		if(ctx.children!=null){
			String ponteiros_opcionais="";
			ponteiros_opcionais=ponteiros_opcionais+visitPonteiros_opcionais(ctx.ponteiros_opcionais());
			ponteiros_opcionais=ponteiros_opcionais+"*";
			return ponteiros_opcionais;
		}
		return "";
	}

	@Override
	public String visitOutros_ident(LAParser.Outros_identContext ctx){
		if(ctx.children!=null)
			return "."+visitIdentificador(ctx.identificador());
		else
			return "";
	}

	@Override
	public String visitDimensao(LAParser.DimensaoContext ctx){
		if(ctx.children!=null){
			String dimensao="";
			dimensao=dimensao+"["+visitExp_aritmetica(ctx.exp_aritmetica())+"]";
			dimensao=dimensao+visitDimensao(ctx.dimensao());
			return dimensao;
		}
		return "";
	}

	@Override 
	public String visitTipo(LAParser.TipoContext ctx){
		if(ctx.registro()!=null)
			return visitRegistro(ctx.registro());
		else
			return visitTipo_estendido(ctx.tipo_estendido());
	}

	@Override
	public String visitMais_ident(LAParser.Mais_identContext ctx){
		if(ctx.children!=null){
			String mais_ident="";
			mais_ident=mais_ident+","+visitIdentificador(ctx.identificador());
			mais_ident=mais_ident+visitMais_ident(ctx.mais_ident());
			return mais_ident;
		}
		return "";
	}

	@Override
	public String visitMais_variaveis(LAParser.Mais_variaveisContext ctx){
		if(ctx.children!=null){
			String mais_variaveis="";
			mais_variaveis=mais_variaveis+visitVariavel(ctx.variavel());
			mais_variaveis=mais_variaveis+visitMais_variaveis(ctx.mais_variaveis());
			return mais_variaveis;
		}
		return "";
	}

	@Override 
	public String visitTipo_basico(LAParser.Tipo_basicoContext ctx){
            switch (ctx.getText()) {
                case "logico":
                    return "boolean";
                case "inteiro":
                    return "int";
                case "real":
                    return "float";
                case "literal":
                    return "char";
                default:
                    break;
            }
		return "";
	}

	@Override
	public String visitTipo_basico_ident(LAParser.Tipo_basico_identContext ctx){
		if(ctx.tipo_basico()!=null)
			return visitTipo_basico(ctx.tipo_basico());
		else
			return ctx.IDENT().toString();
	}

	@Override
	public String visitTipo_estendido(LAParser.Tipo_estendidoContext ctx){	
		if(ctx.children!=null){
			String tipo_estendido="";
			tipo_estendido=tipo_estendido+visitTipo_basico_ident(ctx.tipo_basico_ident());
			tipo_estendido=tipo_estendido+visitPonteiros_opcionais(ctx.ponteiros_opcionais());
			return tipo_estendido;
		}
		return  "";
	}

	@Override
	public String visitValor_constante(LAParser.Valor_constanteContext ctx){
		return ctx.getText();
        }
        
	@Override
	public String visitRegistro(LAParser.RegistroContext ctx){
		if(ctx.children!=null){
			String registro="";
			registro=registro+"struct{\n";
			registro=registro+visitVariavel(ctx.variavel());
			registro=registro+visitMais_variaveis(ctx.mais_variaveis());
			registro=registro+"\t}";
			return registro;
		}
		return "";
	}

	@Override 
	public String visitDeclaracao_global(LAParser.Declaracao_globalContext ctx){
		String declaracao_global="";
		//Procedimento nao tem valor de retorno
		if(ctx.getText().startsWith("procedimento")){
			pilhaTabela.empilhar(new TabelaDeSimbolos("procedimento "+ctx.IDENT().getText()));
			declaracao_global=declaracao_global+"void "+ctx.IDENT().getText()+"(";
			declaracao_global=declaracao_global+visitParametros_opcional(ctx.parametros_opcional())+"){\n";
			declaracao_global=declaracao_global+visitDeclaracoes_locais(ctx.declaracoes_locais());
			declaracao_global=declaracao_global+visitComandos(ctx.comandos());
			declaracao_global=declaracao_global+"}";
			pilhaTabela.desempilhar();
			return declaracao_global;
		}
		//Funcao tem valor de retorno
		else
		{
			pilhaTabela.topo().adicionarSimbolo(ctx.IDENT().getText(),ctx.tipo_estendido().getText());
			pilhaTabela.empilhar(new TabelaDeSimbolos("funcao "+ctx.IDENT().getText()));
			declaracao_global=declaracao_global+"\n"+visitTipo_estendido(ctx.tipo_estendido());
			declaracao_global=declaracao_global+" "+ctx.IDENT().getText()+"(";
			declaracao_global=declaracao_global+visitParametros_opcional(ctx.parametros_opcional())+"){\n";
			declaracao_global=declaracao_global+visitDeclaracoes_locais(ctx.declaracoes_locais());
			declaracao_global=declaracao_global+visitComandos(ctx.comandos());
			declaracao_global=declaracao_global+"}";
			pilhaTabela.desempilhar();
			return declaracao_global;
		}
	}

	@Override
	public String visitParametros_opcional(LAParser.Parametros_opcionalContext ctx){
		if(ctx.children!=null)
			return visitParametro(ctx.parametro());
		return "";
	}

	@Override
	public String visitParametro(LAParser.ParametroContext ctx){
		if(ctx.children!=null){
			String parametros="";
			parametros=parametros+visitVar_opcional(ctx.var_opcional());
			pilhaTabela.topo().adicionarSimbolo(ctx.identificador().IDENT().getText(),visitTipo_estendido(ctx.tipo_estendido()));
			parametros=parametros+visitTipo_estendido(ctx.tipo_estendido());
			if(pilhaTabela.topo().getValorTipoSimbolo(ctx.identificador().IDENT().getText()).equals("char"))
				parametros=parametros+"*";
		parametros=parametros+" "+visitIdentificador(ctx.identificador());
		parametros=parametros+visitMais_ident(ctx.mais_ident());
		parametros=parametros+visitMais_parametros(ctx.mais_parametros());
		return parametros;
		}
		return "";
	}

	@Override
	public String visitVar_opcional(LAParser.Var_opcionalContext ctx){
		return ctx.getText();
	}

	@Override
	public String visitMais_parametros(LAParser.Mais_parametrosContext ctx){
		if(ctx.children!=null)
			return ","+visitParametro(ctx.parametro());
		return "";
	}

	//!!
	@Override
    public String visitDeclaracoes_locais(LAParser.Declaracoes_locaisContext ctx) {
        if(ctx.children != null){
            String declaracoes_locais = "";
            declaracoes_locais += visitDeclaracao_local(ctx.declaracao_local());
            declaracoes_locais += visitDeclaracoes_locais(ctx.declaracoes_locais());
            return declaracoes_locais;
        }
        return "";
    }

    @Override
    public String visitCorpo(LAParser.CorpoContext ctx) {
        //corpo : declaracoes_locais comandos;
        if(ctx.children != null){
            String corpo = "";
            corpo += visitDeclaracoes_locais(ctx.declaracoes_locais());
            corpo += visitComandos(ctx.comandos());
            return corpo;
        }
        return "";
    }

    @Override
    public String visitComandos(LAParser.ComandosContext ctx) {
        //comandos : cmd comandos | ;
        if(ctx.children != null){
            String cmd = "";
            cmd += visitCmd(ctx.cmd());
            cmd += visitComandos(ctx.comandos());
            return cmd;
        }
        return "";
    }

    @Override
    public String visitCmd(LAParser.CmdContext ctx) {
        /*cmd : 'leia' '(' identificador mais_ident ')'
                | 'escreva' '(' expressao mais_expressao ')'
                | 'se' expressao 'entao' comandos senao_opcional 'fim_se'
                | 'caso' exp_aritmetica 'seja' selecao senao_opcional 'fim_caso'
                | 'para' IDENT '<-' exp_aritmetica 'ate' exp_aritmetica 'faca' comandos 'fim_para'
                | 'enquanto' expressao 'faca' comandos 'fim_enquanto'
                | 'faca' comandos 'ate' expressao
                | '^' IDENT outros_ident dimensao '<-' expressao
                | IDENT chamada_atribuicao
                | 'retorne' expressao;*/
        if (ctx.children != null) {
            String cmd = "";
            if(!ctx.nomeCmd.equals("leia")) if (ctx.nomeCmd.equals("escreva")) {
                //'escreva' '(' expressao mais_expressao ')'
                //Se for um simbolo no expressão
                if(pilhaTabela.topo().existeSimbolo(ctx.expressao().getText())) {
                    //Monta a estrutura do printf pegando o tipo de conversão, ex : printf("%d", conta);
                    cmd += "\tprintf(\"";
                    cmd += getTipo(pilhaTabela.topo().getValorTipoSimbolo(ctx.expressao().getText())) + "\",";
                    cmd += visitExpressao(ctx.expressao());
                    cmd += visitMais_expressao(ctx.mais_expressao()) + "); \n";
                }else if(visitExpressao(ctx.expressao()).contains("\"")){ //Se expresão contem texto
                    //Monta a estrutura do printf pegando o tipo de conversão, ex : printf("Conta %d", conta);
                    cmd += "\tprintf(\"";
                    //Retira as aspas do expressão
                    cmd += visitExpressao(ctx.expressao()).replaceAll("\"","");
                    String[] partes = visitMais_expressao(ctx.mais_expressao()).split(",");
                    //Para cada variavel no mais expressão adiciona o tipo de conversão
                    for (String parte : partes) {
                        if (pilhaTabela.topo().existeSimbolo(parte)) {
                            cmd += getTipo(pilhaTabela.topo().getValorTipoSimbolo(parte));
                        }
                    }
                    cmd += "\"";
                    cmd += visitMais_expressao(ctx.mais_expressao()) + "); \n";
                }else if(visitMais_expressao(ctx.mais_expressao()).equals("")){
                    //Se for escreva de retorno de função ou vetor
                    //Divide a expressão e retira os espaços
                    String[] partes = visitExpressao(ctx.expressao()).split("\\+");
                    for(int i = 0; i < partes.length; i++){
                        partes[i] = partes[i].trim();
                    }
                    //Se for função
                    if(partes[0].contains("(")){
                        String[] aux;
                        //Monta a estrutura do printf pegando o tipo de conversão, ex : printf("%d", Conta(numero));
                        cmd += "\tprintf(\"";
                        aux = partes[0].split("\\(");
                        cmd += getTipo(pilhaTabela.topo().getValorTipoSimbolo(aux[0])) + "\",";
                        cmd += partes[0] +");";
                    }else if(partes[0].contains("[")){ //Se for vetor
                        String[] aux;
                        //Monta a estrutura do printf pegando o tipo de conversão, ex : printf("%d", numero[0]);
                        cmd += "\tprintf(\"";
                        aux = partes[0].split("\\[");
                        cmd += getTipo(pilhaTabela.topo().getValorTipoSimbolo(aux[0])) + "\",";
                        cmd += partes[0] +");";
                    }
                    else{
                        cmd += "\tprintf(\"";
                        cmd += getTipo(pilhaTabela.topo().getValorTipoSimbolo(partes[0])) + "\",";
                        cmd += visitExpressao(ctx.expressao()).replaceAll("\\s", "") + "); \n";
                    }
                }else if((visitExpressao(ctx.expressao()).contains("."))){
                    //Se for registro
                    String aux = visitExpressao(ctx.expressao()) + visitMais_expressao(ctx.mais_expressao());
                    String partes[] = aux.split(",");
                    //Monta a estrutura do printf pegando o tipo de conversão, ex : printf("%d", conta.numero);
                    for (String parte : partes) {
                        if (parte.startsWith("\"")) {
                            cmd += "\tprintf(" + parte + ");\n";
                        } else {
                            String[] aux2 = parte.split("\\.");
                            cmd += "\tprintf(\"";
                            cmd += getTipo(pilhaTabela.topo().getValorTipoSimbolo(aux2[1])) + "\",";
                            cmd += parte + ");\n";
                        }
                    }
                }
                return cmd;
            } else if (ctx.nomeCmd.equals("se")){
                //'se' expressao 'entao' comandos senao_opcional 'fim_se'
                //Monta a estrutura do if
                cmd += "\tif (";
                cmd += (visitExpressao(ctx.expressao())).replaceAll("=","==") + ") {\n";
                cmd += "\t" + visitComandos(ctx.comandos()) + "\n\t}";
                //Se senão existir
                if (ctx.senao_opcional().children != null) {
                    cmd += "\telse {";
                    cmd += "\n\t";
                    cmd += visitSenao_opcional(ctx.senao_opcional());
                    cmd += "\n\t}";
                }
                return cmd;
            } else if (ctx.nomeCmd.equals("caso")){
                //'caso' exp_aritmetica 'seja' selecao senao_opcional 'fim_caso'
                //Monta a estrutura do switch
                cmd += "\tswitch (";
                cmd += visitExp_aritmetica(ctx.exp_aritmetica().get(0))+ ") {";
                cmd += visitSelecao(ctx.selecao());
                if (ctx.senao_opcional().children != null) {
                    cmd += "\n\t\tdefault:";
                    cmd += "\n\t\t";
                    cmd += visitSenao_opcional(ctx.senao_opcional());
                }
                cmd += "\n\t}";
                return cmd;
            } else if (ctx.nomeCmd.equals("para")){
                //Monta a estrutura do for
                cmd += "\tfor (i = ";
                cmd +=  visitExp_aritmetica(ctx.exp_aritmetica().get(0)) + "; ";
                cmd += "i <= ";
                cmd += visitExp_aritmetica(ctx.exp_aritmetica().get(1)) + "; i++){\n";
                cmd += visitComandos(ctx.comandos());
                cmd += "\t}\n";
                return cmd;
            } else if (ctx.nomeCmd.equals("enquanto")){
                //'enquanto' expressao 'faca' comandos 'fim_enquanto'
                //Monta a estrutura do while
                cmd += "\twhile (";
                cmd += visitExpressao(ctx.expressao()) + ") {\n";
                cmd += visitComandos(ctx.comandos());
                cmd += "\n\t}";
                return cmd;
            } else if (ctx.nomeCmd.equals("faca")){
                //'faca' comandos 'ate' expressao
                //Monta a estrutura do do while
                cmd += "\t" + "do {\n";
                cmd += visitComandos(ctx.comandos());
                cmd += "\n\t}";
                cmd += "while (" + (visitExpressao(ctx.expressao())).replaceAll("=","==")+"); \n";
                return cmd;
            } else if (ctx.nomeCmd.equals("^")){
                //'^' IDENT outros_ident dimensao '<-' expressao
                //Monta atribuição de ponteiro
                cmd += "\n\t*" + ctx.IDENT().toString();
                cmd += visitOutros_ident(ctx.outros_ident());
                cmd += visitDimensao(ctx.dimensao());
                cmd += " = " + visitExpressao(ctx.expressao()) + ";\n";
                return cmd;
            } else if (ctx.nomeCmd.equals("IDENT")){
                //Se for atribuição de string usa o strcpy
                if(visitChamada_atribuicao(ctx.chamada_atribuicao()).contains(",\"")){
                    cmd += "\tstrcpy(" + ctx.IDENT().toString() + visitChamada_atribuicao(ctx.chamada_atribuicao());
                    cmd += "); \n";
                }else { //Senao normal
                    cmd += "\t" + ctx.IDENT().toString();
                    cmd += visitChamada_atribuicao(ctx.chamada_atribuicao());
                }
                return cmd;
            } else if (ctx.nomeCmd.equals("retorne")){
                cmd += "\treturn ";
                cmd += visitExpressao(ctx.expressao());
                return cmd + ";\n";
            } else {
                //'leia' '(' identificador mais_ident ')'
                //Se for um literal a entrada é feita pelo comando gets
                if ((pilhaTabela.topo().getValorTipoSimbolo(ctx.identificador().getText()).equals("literal")))
                    cmd += "\tgets(";
                else { //senao scanf
                    //Monta a estrutura do scanf pegando o tipo de conversão, ex : scanf("%d",&conta);
                    cmd += "\tscanf(";
                    cmd += "\"" + getTipo(pilhaTabela.topo().getValorTipoSimbolo(ctx.identificador().getText())) + "\"";
                    cmd += ",&";
                }
                cmd += visitIdentificador(ctx.identificador());
                cmd += visitMais_ident(ctx.mais_ident()) + "); \n";
                return cmd;
            }
        }
        return "";
    }

        @Override
    public String visitMais_expressao(LAParser.Mais_expressaoContext ctx) {
        //mais_expressao : ',' expressao mais_expressao | ;
        if(ctx.children != null){
            String mais_expressao = "";
            if(!visitExpressao(ctx.expressao()).startsWith(")"))
                mais_expressao += ",";
            mais_expressao += visitExpressao(ctx.expressao());
            mais_expressao += visitMais_expressao(ctx.mais_expressao());
            return mais_expressao;
        }
        return "";
    }

    @Override
    public String visitSenao_opcional(LAParser.Senao_opcionalContext ctx) {
        //senao_opcional : 'senao' comandos | ;
        if(ctx.children != null) {
            return visitComandos(ctx.comandos());
        }
        return "";
    }

    @Override
    public String visitChamada_atribuicao(LAParser.Chamada_atribuicaoContext ctx) {
        //chamada_atribuicao: '(' argumentos_opcional ')' | outros_ident dimensao '<-' expressao;
        String chamada_atribuicao = "";
        if(ctx.getText().startsWith("(")) {
            chamada_atribuicao += "(" + visitArgumentos_opcional(ctx.argumentos_opcional()) + "); \n";
            return chamada_atribuicao;
        }
        else{
            chamada_atribuicao += visitOutros_ident(ctx.outros_ident());
            //Se for registro
            if(chamada_atribuicao.startsWith(".")){
                String tipoVar = pilhaTabela.topo().getValorTipoSimbolo(chamada_atribuicao.substring(1,chamada_atribuicao.length()));
                if(tipoVar.equals("literal")){
                    chamada_atribuicao += visitDimensao(ctx.dimensao()) + ",";
                    chamada_atribuicao += visitExpressao(ctx.expressao());
                    return chamada_atribuicao;
                }
            }
            chamada_atribuicao += visitDimensao(ctx.dimensao()) + " = ";
            chamada_atribuicao += visitExpressao(ctx.expressao()) + "; \n";
            return chamada_atribuicao;
        }
    }

    @Override
    public String visitArgumentos_opcional(LAParser.Argumentos_opcionalContext ctx) {
        //argumentos_opcional: expressao mais_expressao | ;
        if(ctx.children != null){
            String argumentos_opcional = "";
            argumentos_opcional += visitExpressao(ctx.expressao());
            argumentos_opcional += visitMais_expressao(ctx.mais_expressao());
            return argumentos_opcional;
        }
        return "";
    }

    @Override
    public String visitSelecao(LAParser.SelecaoContext ctx) {
        //selecao: constantes ':' comandos mais_selecao;
        if(ctx.children != null){
            //Monta a estrutura do case;
            String selecao = "";
            selecao += visitConstantes(ctx.constantes());
            selecao += "\n\t\t";
            selecao += visitComandos(ctx.comandos());
            selecao += "\n\t\t\tbreak;";
            selecao += visitMais_selecao(ctx.mais_selecao());
            return selecao;
        }
        return "";
    }

    @Override
    public String visitMais_selecao(LAParser.Mais_selecaoContext ctx) {
        //mais_selecao: selecao | ;
        if (ctx.children != null)
            return visitSelecao(ctx.selecao());
        return "";
    }

    @Override
    public String visitConstantes(LAParser.ConstantesContext ctx) {
        //constantes: numero_intervalo mais_constantes;
        if (ctx.children != null) {
            String constantes = "";
            constantes += visitNumero_intervalo(ctx.numero_intervalo());
            constantes += visitMais_constantes(ctx.mais_constantes());
            return constantes;
        }
        return "";
    }

    @Override
    public String visitMais_constantes(LAParser.Mais_constantesContext ctx) {
        // mais_constantes: ',' constantes | ;
        if (ctx.children != null)
            return ',' + visitConstantes(ctx.constantes());
        return "";
    }

    @Override
    public String visitNumero_intervalo(LAParser.Numero_intervaloContext ctx) {
        // numero_intervalo: op_unario NUM_INT intervalo_opcional;
        if (ctx.children != null) {
            String numero_intervalo = "";
            numero_intervalo += visitOp_unario(ctx.op_unario());
            //numero_intervalo += ctx.NUM_INT().toString();
            numero_intervalo += visitIntervalo_opcional(ctx.intervalo_opcional(), Integer.parseInt(ctx.NUM_INT().toString()));
            return numero_intervalo;
        }
        return "";
    }

    //@Override
    //Como há a necessidade do intervalo do case a função não foi sobreescrita
    public String visitIntervalo_opcional(LAParser.Intervalo_opcionalContext ctx, int comeco) {
        // intervalo_opcional: '..' op_unario NUM_INT | ;
        String intervalo_opcional= "";
        //No intervalo do case é gerado as entradas
        if (ctx.children != null) {
            intervalo_opcional += visitOp_unario(ctx.op_unario());
            //intervalo_opcional += ctx.NUM_INT().toString();
            for(int i = comeco; i <= Integer.parseInt(ctx.NUM_INT().toString()); i++){
                intervalo_opcional += "\n\t\tcase " + i + ":";
            }
            return intervalo_opcional;
        }else {
            intervalo_opcional += "\n\t\tcase " + comeco +":";
            return intervalo_opcional;
        }
    }

    @Override
    public String visitOp_unario(LAParser.Op_unarioContext ctx) {
        //op_unario: '-' | ;
        return ctx.getText();
    }

    @Override
    public String visitExp_aritmetica(LAParser.Exp_aritmeticaContext ctx) {
        //exp_aritmetica: termo outros_termos;
        if(ctx.children != null){
            String exp_aritmetica = "";
            exp_aritmetica += visitTermo(ctx.termo());
            exp_aritmetica += visitOutros_termos(ctx.outros_termos());
            return exp_aritmetica;
        }
        return "";
    }

    @Override
    public String visitOp_multiplicacao(LAParser.Op_multiplicacaoContext ctx) {
        //op_multiplicacao: '*'| '/';
        return ctx.getText();
    }

    @Override
    public String visitOp_adicao(LAParser.Op_adicaoContext ctx) {
        //op_adicao : '+' | '-';
        return " " + ctx.getText() + " ";
    }

    @Override
    public String visitTermo(LAParser.TermoContext ctx) {
        //termo : fator outros_fatores;
        if(ctx.children != null){
            String termo = "";
            termo += visitFator(ctx.fator());
            termo += visitOutros_fatores(ctx.outros_fatores());
            return termo;
        }
        return "";
    }

    @Override
    public String visitOutros_termos(LAParser.Outros_termosContext ctx) {
        //outros_termos : op_adicao termo outros_termos | ;
        if (ctx.children != null) {
            String outros_termos = "";
            outros_termos += visitOp_adicao(ctx.op_adicao());
            outros_termos += visitTermo(ctx.termo());
            outros_termos += visitOutros_termos(ctx.outros_termos());
            return outros_termos;
        }
        return "";
    }

    @Override
    public String visitFator(LAParser.FatorContext ctx) {
        //fator : parcela outras_parcelas;
        if(ctx.children != null){
            String Fator = "";
            Fator += visitParcela(ctx.parcela());
            Fator += visitOutras_parcelas(ctx.outras_parcelas());
            return Fator;
        }
        return "";
    }

    @Override
    public String visitOutros_fatores(LAParser.Outros_fatoresContext ctx){
        //outros_fatores : op_multiplicacao fator outros_fatores | ;
        if (ctx.children != null) {
            String outros_fatores = "";
            outros_fatores += visitOp_multiplicacao(ctx.op_multiplicacao());
            outros_fatores += visitFator(ctx.fator());
            outros_fatores += visitOutros_fatores(ctx.outros_fatores());
            return outros_fatores;
        }
        return "";
    }

    @Override
    public String visitParcela(LAParser.ParcelaContext ctx) {
        //parcela : op_unario parcela_unario | parcela_nao_unario;
        if (ctx.op_unario() != null) {
            String parcela = "";
            parcela += visitOp_unario(ctx.op_unario());
            parcela += visitParcela_unario(ctx.parcela_unario());
            return parcela;
        } else
            return visitParcela_nao_unario(ctx.parcela_nao_unario());
    }

    @Override
    public String visitParcela_unario(LAParser.Parcela_unarioContext ctx) {
        /*parcela_unario : '^' IDENT outros_ident dimensao
                | IDENT chamada_partes
                | NUM_INT
                | NUM_REAL
                | '(' expressao ')';*/
        String parcela_unario = "";
        if (ctx.getText().startsWith("^")) {
            parcela_unario += "*";
            parcela_unario += ctx.IDENT().toString();
            parcela_unario += visitOutros_ident(ctx.outros_ident());
            parcela_unario += visitDimensao(ctx.dimensao());
            return parcela_unario;
        } else if (ctx.chamada_partes() != null) {
                return ctx.IDENT().toString() + visitChamada_partes(ctx.chamada_partes());
        } else if (ctx.getText().startsWith("(")) {
            parcela_unario += "(";
            parcela_unario += visitExpressao(ctx.expressao());
            parcela_unario += ")";
            return parcela_unario;
        } else if (ctx.NUM_INT() != null)
            return ctx.NUM_INT().toString();
        else if (ctx.NUM_REAL() != null)
            return ctx.NUM_REAL().toString();
        return "";
    }

    @Override
    public String visitParcela_nao_unario(LAParser.Parcela_nao_unarioContext ctx) {
        //parcela_nao_unario : '&' IDENT outros_ident dimensao | CADEIA;
        if (ctx.outros_ident() != null) {
            String parcela_nao_unario = "";
            parcela_nao_unario += "&" + ctx.IDENT().toString();
            parcela_nao_unario += visitOutros_ident(ctx.outros_ident());
            parcela_nao_unario += visitDimensao(ctx.dimensao());
            return parcela_nao_unario;
        }else {
            //Caso exista um \n ou e no final coloca o /n em outra linha com o printf
            if(ctx.CADEIA().toString().equals("\"\\n\"") || ctx.CADEIA().toString().equals("\" e \""))
                return ");\n\tprintf(" + ctx.CADEIA().toString();
            return ctx.CADEIA().toString();
        }
    }

    @Override
    public String visitOutras_parcelas(LAParser.Outras_parcelasContext ctx) {
        //outras_parcelas : '%' parcela outras_parcelas | ;
        if (ctx.children != null) {
            String outras_parcelas = "";
            outras_parcelas += "&" + visitParcela(ctx.parcela());
            outras_parcelas += visitOutras_parcelas(ctx.outras_parcelas());
            return outras_parcelas;
        }
        return "";
    }

    @Override
    public String visitChamada_partes(LAParser.Chamada_partesContext ctx) {
        // chamada_partes :  '(' expressao mais_expressao ')'  | outros_ident dimensao | ;
        String chamada_partes = "";
        if (ctx.expressao() != null) {
            chamada_partes += "(";
            chamada_partes += visitExpressao(ctx.expressao());
            chamada_partes += visitMais_expressao(ctx.mais_expressao());
            chamada_partes += ")";
            return chamada_partes;
        } else
        if (ctx.outros_ident() != null) {
            chamada_partes += visitOutros_ident(ctx.outros_ident());
            chamada_partes += visitDimensao(ctx.dimensao());
            return chamada_partes;
        }
        return "";
    }

    @Override
    public String visitExp_relacional(LAParser.Exp_relacionalContext ctx) {
        //exp_relacional : exp_aritmetica op_opcional;
        if(ctx.children != null){
            String exp_relacional = "";
            exp_relacional += visitExp_aritmetica(ctx.exp_aritmetica());
            exp_relacional += visitOp_opcional(ctx.op_opcional());
            return exp_relacional;
        }
        return "";
    }

    @Override
    public String visitOp_opcional(LAParser.Op_opcionalContext ctx) {
        //op_opcional : op_relacional exp_aritmetica | ;
        if (ctx.children != null) {
            String op_opcional = "";
            op_opcional += visitOp_relacional(ctx.op_relacional());
            op_opcional += visitExp_aritmetica(ctx.exp_aritmetica());
            return op_opcional;
        }
        return "";
    }

    @Override
    public String visitOp_relacional(LAParser.Op_relacionalContext ctx) {
        //op_relacional : '=' | '<>'| '>=' | '<=' | '>' | '<';
        return " " + ctx.getText() + " ";
    }

    @Override
    public String visitExpressao(LAParser.ExpressaoContext ctx) {
        //expressao : termo_logico outros_termos_logicos;
        if(ctx.children != null){
            String expressao = "";
            expressao += visitTermo_logico(ctx.termo_logico());
            expressao += visitOutros_termos_logicos(ctx.outros_termos_logicos());
            return expressao;
        }
        return "";
    }

    @Override
    public String visitOp_nao(LAParser.Op_naoContext ctx) {
        //op_nao : 'nao' | ;
        if(ctx.getText().equals("nao"))
            return "!";
        return "";
    }

    @Override
    public String visitTermo_logico(LAParser.Termo_logicoContext ctx) {
        //termo_logico : fator_logico outros_fatores_logicos;
        if(ctx.children != null){
            String termo_logico = "";
            termo_logico += visitFator_logico(ctx.fator_logico());
            termo_logico += visitOutros_fatores_logicos(ctx.outros_fatores_logicos());
            return termo_logico;
        }
        return "";
    }

    @Override
    public String visitOutros_termos_logicos(LAParser.Outros_termos_logicosContext ctx) {
        // outros_termos_logicos : 'ou' termo_logico outros_termos_logicos | ;
        if (ctx.children != null) {
            String outros_termos_logicos = "";
            outros_termos_logicos += "|| " + visitTermo_logico(ctx.termo_logico());
            outros_termos_logicos += visitOutros_termos_logicos(ctx.outros_termos_logicos());
            return outros_termos_logicos;
        }
        return "";
    }

    @Override
    public String visitOutros_fatores_logicos(LAParser.Outros_fatores_logicosContext ctx) {
        // outros_fatores_logicos : 'e' fator_logico outros_fatores_logicos | ;
        if (ctx.children != null) {
            String outros_fatores_logicos = "";
            outros_fatores_logicos += "&& " + visitFator_logico(ctx.fator_logico());
            outros_fatores_logicos += visitOutros_fatores_logicos(ctx.outros_fatores_logicos());
            return outros_fatores_logicos;
        }
        return "";
    }

    @Override
    public String visitFator_logico(LAParser.Fator_logicoContext ctx) {
        //fator_logico : op_nao parcela_logica;
        if(ctx.children != null){
            String fator_logico = "";
            fator_logico += visitOp_nao(ctx.op_nao());
            fator_logico += visitParcela_logica(ctx.parcela_logica());
            return fator_logico;
        }
        return "";
    }

    @Override
    public String visitParcela_logica(LAParser.Parcela_logicaContext ctx) {
        //parcela_logica : 'verdadeiro' | 'falso' | exp_relacional;
        if(ctx.exp_relacional() != null){
            return visitExp_relacional(ctx.exp_relacional());
        }else{
            return ctx.getText();
        }
    }
}