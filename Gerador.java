import java.lang.reflect.Array;
import java.util.ArrayList;

public class Gerador extends LABaseVisitor<String>{
	
	private SaidaParser sp;
	private PilhaDeTabelas pilhaTabela;
	private String tipoMaisVar;	

	//public GeradorCodigo(SaidaParser sp){this.sp = sp;}
	//?

	public String getTipo(String tipo){
		if(tipo.equals("inteiro"))
			return "%d";
		else if(tipo.equals("real"))
			return "%f";
		else if(tipo.equals("literal") || tipo.equals("char"))
			return "%s";
		return "";
	}

	@Override
	public String visitPrograma(LAParser.ProgramaContext ctx){
		if(ctx.children != null){
			pilhaTabela = new PilhaDeTabelas();
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
			declaracoes = declaracoes + visitDecl_local_global(ctx.decl_local_global());
			declaracoes = declaracoes + visitDeclaracoes(ctx.declaracoes());
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
	public String visitDeclaracao_local()