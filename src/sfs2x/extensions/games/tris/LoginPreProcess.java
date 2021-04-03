/**
 * 
 */
package sfs2x.extensions.games.tris;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.components.login.ILoginAssistantPlugin;
import com.smartfoxserver.v2.components.login.LoginData;
import com.smartfoxserver.v2.components.login.PasswordCheckException;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSLoginException;

import sfs2x.extensions.games.model.Users;

/**
 * @author Shashank Panwar
 *
 */
public class LoginPreProcess implements ILoginAssistantPlugin {
	 private final EntityManager em;
	 
	/**
	 * @param em
	 */
	public LoginPreProcess(EntityManager em) {
		super();
		this.em = em;
	}

//	@Override
//	public void execute(LoginData ld) throws Exception {
//		String clientPass = ld.clientIncomingData.getUtfString("password");
//		String username = ld.userName.toString();
//		// Let's see if the password from the DB matches that of the user
//		IDBManager dbManager = TrisExtension.sfs2xDB;
//		Connection connection = null;
//		System.out.println("!@#!@#!@# LoginPreProcess dbManager - " + dbManager + "   username - " + username
//				+ " ld.password-" + ld.password + " clientPass- " + clientPass);
//		try {
//			connection = dbManager.getConnection();
//			System.out.println("!@#!@#!@# LoginPreProcess connection - " + connection);
//			PreparedStatement stmt = connection.prepareStatement("SELECT password,id FROM users WHERE name=?");
//			stmt.setString(1, username);
//			ResultSet res = stmt.executeQuery();
//			ISession session = (ISession) ld.session;
//			while (res.next()) {
//				String password = res.getString("password");
//				int id = res.getInt("id");
//				System.out.println("Password from DB is - " + password + " and id -" + id);
//				if (password != null && !password.isEmpty()) {
//					System.out.println("###### Record is found");
//
//					if (clientPass != null && !clientPass.isEmpty()) {
//						if (!password.trim().equals(clientPass.trim()))
//						{
//							System.out.println("************* PasswordCheckException   *****************");
//							throw new PasswordCheckException();
//						}
//						
//						session.setProperty(TrisExtension.DATABASE_ID, id);
//						System.out.println("************* TrisExtension.DATABASE_ID  -"+TrisExtension.DATABASE_ID+"--- "+id);
//					}
//				} else {
//					session.setProperty(TrisExtension.DATABASE_ID, 0);
//					System.out.println("###### Record is not found");
//					SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
//					errData.addParameter(username);
//					throw new SFSLoginException("Bad user name: " + username, errData);
//				}
//			}
//
//		} catch (Exception e) {
//			System.out.println("###### Exception inside the LoginPreProcess.class is - " + e.getMessage());
//		}
//	}
	
	
	//JPA
	
	@Override
	public void execute(LoginData ld) throws Exception {
		String clientPass = ld.clientIncomingData.getUtfString("password");
		String username = ld.userName.toString();
		// Let's see if the password from the DB matches that of the user
		IDBManager dbManager = TrisExtension.sfs2xDB;
		System.out.println("!@#!@#!@# LoginPreProcess em - " + em + "   username - " + username
				+ " ld.password-" + ld.password + " clientPass- " + clientPass);
		try {
			CriteriaBuilder builder = em.getCriteriaBuilder();
			CriteriaQuery<Users> crQuery = builder.createQuery(Users.class);
			Root<Users> root = crQuery.from(Users.class);
			crQuery.select(root).where(builder.equal(root.get("name"), username));
			System.out.println("!@#!@#!@# LoginPreProcess root - " + root);
			Query query = em.createQuery(crQuery);
			Users user = (Users) query.getResultList().stream().findFirst().orElse(null);
			ISession session = (ISession) ld.session;
			if(user != null)
			{
				String password = user.getPassword();
				int id = user.getId();
				System.out.println("Password from DB is - " + password + " and id -" + id);
				if (password != null && !password.isEmpty()) {
					System.out.println("###### Record is found");

					if (clientPass != null && !clientPass.isEmpty()) {
						if (!password.trim().equals(clientPass.trim()))
						{
							System.out.println("************* PasswordCheckException   *****************");
							throw new PasswordCheckException();
						}
						
						session.setProperty(TrisExtension.DATABASE_ID, id);
						System.out.println("************* TrisExtension.DATABASE_ID  -"+TrisExtension.DATABASE_ID+"--- "+id);
					}
				} else {
					session.setProperty(TrisExtension.DATABASE_ID, 0);
					System.out.println("###### Record is not found");
					SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
					errData.addParameter(username);
					throw new SFSLoginException("Bad user name: " + username, errData);
				}
			}
			else
			{
				session.setProperty(TrisExtension.DATABASE_ID, 0);
				System.out.println("###### Record is not found");
				SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
				errData.addParameter(username);
				throw new SFSLoginException("Bad user name: " + username, errData);
			}

		} catch (Exception e) {
			System.out.println("###### Exception inside the LoginPreProcess.class is - " + e.getMessage());
			SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
			errData.addParameter(username);
			throw new SFSLoginException("Bad password " + username, errData);
		}
	}

}
