/*
 * Icegem, Extensions library for VMWare vFabric GemFire
 * 
 * Copyright (c) 2010-2011, Grid Dynamics Consulting Services Inc. or third-party  
 * contributors as indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  
 * 
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License v3, as published by the Free Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * You should have received a copy of the GNU Lesser General Public License v3
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.googlecode.icegem.cacheutils.monitor.utils;

import com.googlecode.icegem.utils.PropertiesHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Service to simplify the email sending
 */
public class EmailService {

	private static EmailService instance = null;

	private Session mailSession;

	private PropertiesHelper propertiesHelper;

	public static EmailService getInstance() {
		if (instance == null) {
			try {
				instance = new EmailService();
			} catch (Exception e) {
				throw new IllegalStateException(
						"Cannot initialize EmailService", e);
			}
		}

		return instance;
	}

	/**
	 * Sends email with subject and content to receivers specified in property file after key "mail.to" 
	 * 
	 * @param subject - the subject
	 * @param content - the content
	 * @throws MessagingException
	 */
	public void send(String subject, String content) throws MessagingException {
		MimeMessage message = compose(subject, content,
				propertiesHelper.getStringProperty("mail.to"));
		transport(message);
	}

	/**
	 * Sends email with subject and content to receivers specified by argument "to"
	 * 
	 * @param subject - the subject
	 * @param content - the content
	 * @param to - the CSV list of receivers
	 * @throws MessagingException
	 */
	public void send(String subject, String content, String to)
			throws MessagingException {
		MimeMessage message = compose(subject, content, to);
		transport(message);
	}

	private EmailService() throws FileNotFoundException, IOException {
		propertiesHelper = new PropertiesHelper("/monitoring.properties");
		mailSession = Session.getDefaultInstance(
				propertiesHelper.getProperties(),
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(propertiesHelper
								.getStringProperty("mail.user"),
								propertiesHelper
										.getStringProperty("mail.password"));
					}
				});
	}

	private Set<String> csvToSetOfString(String csv) {
		Set<String> resultSet = new HashSet<String>();

		if ((csv != null) && (csv.trim().length() > 0)) {
			for (String s : csv.split(",")) {
				resultSet.add(s.trim());
			}
		}

		return resultSet;
	}

	private MimeMessage compose(String subject, String content, String to)
			throws MessagingException {
		MimeMessage message = new MimeMessage(mailSession);
		message.setSubject(subject);
		message.setContent(content, "text/html; charset=ISO-8859-1");

		message.setFrom(new InternetAddress(propertiesHelper
				.getStringProperty("mail.from")));
		for (String email : csvToSetOfString(to)) {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					email));
		}

		return message;
	}

	private void transport(MimeMessage message) throws MessagingException {
		Transport.send(message);
	}
}
