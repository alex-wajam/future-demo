// Fake DB Interface in Scala
// Made to show my point about Futures in Scala
// Spoiler: they rock!

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait DbData {
  case class User(id: Int, firstName: String, lastName: String)
  type IdSet = Iterable[Int]
}

trait DbActions extends DbData {
  def getWithId(id: Int): Option[User]
  def getAll(): Iterable[User]

  def getUserById(id: Int): Future[User]
  def getUsersByFirstName(firstName: String): Future[IdSet]
  def getUsersByLastName(lastName: String): Future[IdSet]
}


trait DbImpl extends DbActions {

  def getUserById(id: Int): Future[User] = Future {
    Thread.sleep(2000)
    getWithId(id) match {
      case Some(user) => user
      case None => throw new Exception("User not found")
    }
  }

  private def filterUser(userFilter: (User) => Boolean): Future[IdSet] = Future {
    Thread.sleep(5000)
    for {
      user <- getAll()
      if userFilter(user)
    } yield user.id
  }

  def getUsersByFirstName(firstName: String): Future[IdSet] = filterUser(_.firstName == firstName)
  def getUsersByLastName(lastName: String): Future[IdSet] = filterUser(_.lastName == lastName)
}

trait StaticDb extends DbActions {
  object StaticDbValues {
    val users = 
      User(1, "Alex", "Bergeron") ::
      User(2, "Alex", "Marty") ::
      User(3, "Olivier", "Toupin") ::
      User(4, "Olivier", "Contant") ::
      User(5, "Robert", "Brockie") ::
      User(6, "Robert", "Vincent") ::
      User(7, "Felix", "Trepanier") ::
      User(8, "Manuel", "Laflamme") ::
      User(9, "Jérome", "Gagnon") :: Nil
  }
  
  def getWithId(id: Int): Option[User] = (for {
    u <- StaticDbValues.users
    if u.id == id
  } yield u).headOption
  def getAll(): Iterable[User] = StaticDbValues.users
}

object DB extends StaticDb with DbImpl with DbActions with DbData 
