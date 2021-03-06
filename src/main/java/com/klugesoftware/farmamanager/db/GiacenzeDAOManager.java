package com.klugesoftware.farmamanager.db;

import com.klugesoftware.farmamanager.model.Giacenze;

public class GiacenzeDAOManager {

	/**
	 * se ritorna un valore = 0 allora la tabella delle Giacenze è vuota e quindi va aggiornata
	 */
	public static int countGiacenze(){
		DAOFactory daoFactory = DAOFactory.getInstance();
		GiacenzeDAO giacenzaDAO = daoFactory.getGiacenzeDAO();
		return giacenzaDAO.countTable();
	}
	
	public static Giacenze insert(Giacenze giacenza){
		DAOFactory daoFactory = DAOFactory.getInstance();
		GiacenzeDAO giacenzaDAO = daoFactory.getGiacenzeDAO();
		return giacenzaDAO.create(giacenza);
	}

	public static Giacenze findById(int idGiacenza){
		DAOFactory daoFactory = DAOFactory.getInstance();
		GiacenzeDAO giacenzaDAO = daoFactory.getGiacenzeDAO();
		return giacenzaDAO.findById(idGiacenza);
	}
	
	public static Giacenze findByMinsan(String minsan){
		DAOFactory daoFactory = DAOFactory.getInstance();
		GiacenzeDAO giacenzaDAO = daoFactory.getGiacenzeDAO();
		return giacenzaDAO.findByMinsan(minsan);		
	}

	public static Giacenze update(Giacenze giacenza){
		DAOFactory daoFactory = DAOFactory.getInstance();
		GiacenzeDAO giacenzaDAO = daoFactory.getGiacenzeDAO();
		return giacenzaDAO.update(giacenza);		
	}

	public static Giacenze updateByMinsan(Giacenze giacenza){
		DAOFactory daoFactory = DAOFactory.getInstance();
		GiacenzeDAO giacenzaDAO = daoFactory.getGiacenzeDAO();
		return giacenzaDAO.updateByMinsan(giacenza);
	}

	public static void deleteTable(){
		DAOFactory daoFactory = DAOFactory.getInstance();
		GiacenzeDAO giacenzaDAO = daoFactory.getGiacenzeDAO();
		giacenzaDAO.deleteTable();
	}
}
