import java.lang.reflect.Array;
import java.util.ArrayList;

public class Gerador extends LABaseVisitor<String>{
	
	private SaidaParser sp;
	private PilhaDeTabelas pilhaTabela;
	private String tipoMaisVar;	

	public GeradorCodigo(SaidaParser sp){
		this.sp=sp;
	}

	public String getTipo(String tipo){
		if(tipo.equals("inteiro"))
			return "%d";
		else if(tipo.equals("real"))
			return "%f";
		else if(tipo.equals("literal")||tipo.equals("char"))
			return "%s";
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
			sp.prinln(visitCorpo(ctx.corpo()));

			sp.println("\treturn 0;\n}");
			pilhaTabela.desempilhar();
		}
		return "";
	}

	@Override
	public String visitDeclaracoes(LAParser.DeclaracoesContext ctx){
		if(ctx.children!=NULL){
			String declaracoes = "";
			declaracoes=declaracoes+visitDecl_local_global(ctx.decl_local_global());
			declaracoes=declaracoes+visitDeclaracoes(ctx.declaracoes());
			return declaracoes;
		}
		return "";
	}

	@Override
	public String visitDecl_local_global(LAParser.Decl_local_globalCtx ctx){
		if(ctx.declaracao_local()!=NULL)
			return visitDeclaracao_local(ctx.declaracao_local());
		else if(ctx.declaracao_global()!=NULL)
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
		if(ctx.children!=NULL){
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
		if(ctx.children!=NULL){
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
		if(ctx.children!=NULL){
			String ponteiros_opcionais="";
			ponteiros_opcionais=ponteiros_opcionais+visitPonteiros_opcionais(ctx.ponteiros_opcionais());
			ponteiros_opcionais=ponteiros_opcionais+"*";
			return ponteiros_opcionais;
		}
		return "";
	}

	@Override
	public String visitOutros_ident(LAParser.Outros_identContext ctx){
		if(ctx.children!=NULL)
			return "."+visitIdentificador(ctx.identificador());
		else
			return "";
	}

	@Override
	public String visitDimensao(LAParser.DimensaoContext ctx){
		if(ctx.children!=NULL){
			String dimensao="";
			dimensao=dimensao+"["+visitExp_aritmetica(ctx.exp_aritmetica())+"]";
			dimensao=dimensao+visitDimensao(ctx.dimensao());
			return dimensao;
		}
		return "";
	}

	@Override 
	public String visitTipo(LAParser.TipoContext ctx){
		if(ctx.registro()!=NULL)
			return visitRegistro(ctx.registro());
		else
			return visitTipo_estendido(ctx.tipo_estendido());
	}

	@Override
	public String visitMais_ident(LAParser.Mais_identContext ctx){
		if(ctx.children!=NULL){
			String mais_ident="";
			mais_ident=mais_ident+","+visitIdentificador(ctx.identificador());
			mais_ident=mais_ident+visitMais_ident(ctx.mais_ident());
			return mais_ident;
		}
		return "";
	}

	@Override
	public String visitMais_variaveis(LAParser.Mais_variaveisContext ctx){
		if(ctx.children!=NULL){
			String mais_variaveis="";
			mais_variaveis=mais_variaveis+visitVariavel(ctx.variavel());
			mais_variaveis=mais_variaveis+visitMais_variaveis(ctx.mais_variaveis());
			return mais_variaveis;
		}
		return "";
	}

	@Override 
	public String visitTipo_basico(LAParser.Tipo_basicoContext ctx){
		if(ctx.getText().equals("logico"))
			return "boolean";
		else if(ctx.getText().equals("inteiro"))
			return "int";
		else if(ctx.getText().equals("real"))
			return "float";
		else if(ctx.getText().equals("literal"))
			return "char";
		return "";
	}

	@Override
	public String visitTipo_basico_ident(LAParser.Tipo_basico_identContext ctx){
		if(ctx.tipo_basic()!=NULL)
			return visitTipo_basic(ctx.tipo_basico());
		else
			return ctx.IDENT().toString();
	}

	@Override
	public String visitTipo_estendido(LAParser.Tipo_estendidoContext ctx){	
		if(ctx.children!=NULL){
			String tipo_estendido="";
			tipo_estendido=tipo_estendido+visitTipo_basico_ident(ctx.tipo_basico_ident());
			tipo_estendido=tipo_estendido+visitPonteiros_opcionais(ctx.ponteiros_opcionais());
			return tipo_estendido;
		}
		return  "";
	}

	@Override
	public String visitValor_constante(LAParser.Valor_constanteContext ctx)
		return ctx.getText();

	@Override
	public String visitRegistro(LAParser.RegistroContext ctx){
		if(ctx.children!=NULL){
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
		if(ctx.children!=NULL)
			return visitParametro(ctx.parametro());
		return "";
	}

	@Override
	public String visitParametro(LAParser.ParametroContext ctx){
		
	}


}
