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
			declaracao_local=declaracao_locao+visitVariavel(ctx.variavel());
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

	
