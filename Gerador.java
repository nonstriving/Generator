import java.lang.reflect.Array;
import java.util.ArrayList;

public class Gerador extends LABaseVisitor<String>{
	
	private SaidaParser sp;
	private PilhaDeTabelas pilhaTabela;
	private String tipoMaisVar;	

	public String getTipo(String tipo){
		if(tipo.equals("inteiro"))
			return "%d";
		else if(tipo.equals("real"))
			return "%f";
		else if(tipo.equals("literal") || tipo.equals("char"))
			return "%s";
		return "";
	}
