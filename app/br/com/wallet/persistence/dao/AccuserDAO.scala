package br.com.wallet.persistence.dao

import br.com.wallet.persistence.dto.AccuserDto

import anorm.SqlParser._

/**
 * Created by sirkleber on 4/6/15.
 */
object AccuserDAO extends Dao {

  def getAccusers(accuserid: String) = queryRunnerSingle(
    "Select count(usermail) from accuser where usermail = {usermail}"
  )(int("count"))("accuserid" -> accuserid).map(_ > 0)

  def insertAccuser(useracc: AccuserDto) = useracc match {
    case AccuserDto(id, mail) =>
      lazy val query = "Insert into accuser(accuserid, usermail) values({accuserid, usermail})"
      queryUpdate(query)("accuserid" -> id, "usermail" -> mail)
  }
}
