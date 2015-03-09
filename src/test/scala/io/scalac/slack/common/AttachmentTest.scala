package io.scalac.slack.common

import org.scalatest.{Matchers, FunSuite}

/**
 * Created on 09.03.15 10:35
 */
class AttachmentTest extends FunSuite with Matchers {

  test("parse attachment with color only") {
    val elems = List[RichMessageElement](Color.good)
    val att1 = Attachment(elems)
    val att = Attachment(color = Some("good"))
    att should equal (att1)
    att shouldNot be ('valid)
  }

  test("parse attachment with title ") {
    val elems = List[RichMessageElement](Title("fine title"))
    val att1 = Attachment(elems)
    val att = Attachment(title = Some("fine title"))
    att should equal (att1)
    att should be ('valid)
  }

  test("parse attachment with titleURL only ") {
    val elems = List[RichMessageElement](Title("", Some("title url")))
    val att1 = Attachment(elems)
    val att = Attachment()
    att should equal (att1)
    att shouldNot be ('valid)
  }

  test("parse attachment with title and titleURL  ") {
    val elems = List[RichMessageElement](Title("title", Some("title url")))
    val att1 = Attachment(elems)
    val att = Attachment(title = Some("title"), titleURL = Some("title url"))
    att should equal (att1)
    att should be ('valid)
  }
  test("parse attachment with title ,titleURL and Color") {
    val elems = List[RichMessageElement](Title("title", Some("title url")), Color.warning)
    val att1 = Attachment(elems)
    val att = Attachment(title = Some("title"), titleURL = Some("title url"), color = Some("warning"))
    att should equal (att1)
    att should be ('valid)
  }

  test("parse fields and pretext"){
    val elems = List[RichMessageElement](PreText("pretext"), Field("title 1", "content 1", short = false))
    val att1 = Attachment(elems)
    val att = Attachment(preText = Some("pretext"), fields = Some(List(Field("title 1", "content 1", short = false))))
    att should equal (att1)
    att should be ('valid)
  }

  test("parse some fields and text"){
    val elems = List[RichMessageElement](Text("sometext"), Field("title 1", "content 1", short = false), Field("title 2", "content 2", true))
    val att1 = Attachment(elems)
    val att = Attachment(text = Some("sometext"), fields = Some(List(Field("title 1", "content 1", short = false), Field("title 2", "content 2", true))))
    att should equal (att1)
    att should be ('valid)
  }

}
