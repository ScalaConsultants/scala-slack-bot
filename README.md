# scala-slack-bot


Copy `application.conf` as `secret.conf` and fill the gaps.

`api.key` - slack integration key
`websocket.key` - encoded base64 UUID, for unique websocket connection


##How to Listen

Extend `OutgoingMessageListener` or `IncomingMessageListener`, first if you want catch everything sent from other listeners, second
if you want to catch everything from channel.
All you match inside match block is what you want to catch.

##how to catch message

You need to listen to `BaseMessage` case class.

##How to find command?

If your bot has name _zizzi_ and command _links_ with params _delete_ _1_ you can call it in this ways:

`$links delete 1`
`@zizzi links delete 1`
`@zizzi: links delete 1`

all these things will be translated into object: `Command("links", List("delete", "1"), um)`
**um** - is underlaying `BaseMessage` object for additional info.

##How to send message?

use `publish` function inside listeners.

##What can I send?

There are two types of messages you can send.

If you want to send basic, plain message use `OutboundMessage` case class. It has two arguments: channel and message.

    publish(OutboundMessage(channel, s"hello <@$user>, welcome"))

If you add colors or line breaks, you can send rich message. To do so, you need to send message to channel and add one or more attachments:

![Rich message example](../master/richmessage.png?raw=true)

on this image you see Rich message with 3 attachment, each attachment has its own color decorator, default is grey what is seen in second attachment.

Attachment can be build from:
- **PreText** - text block before colored block,
- **Title** - bold text in first line of colored block, you can optionally add URL if you want title as link.
- **Text** - text displayed under the title
- **Color** - color of left border of block, you can use any in hex format `#RRGGBB` or predefined: `Color.good`, `Color.warning` or `Color.danger`
- **Field** - you can add as many Fields as you want. Every field is build from title, value and boolean value that describe the field should be displayed one per row.
  In exaple aobve there are 4 fields described. First and 4th are long (one per row) and the 2nd & 3rd are short(placed in one row).
  Field's title is bold.

You can use any combination of these.

code for example above:

    publish(RichOutboundMessage(m.channel, List(
            Attachment(
              Title("Hello title, should be link to Scalac's page", Some("http://scalac.io")),
              Color.danger,
              PreText("this is PreText"),
              Text("Now I'm talking with color and blocks"),
              Field("Field 1", "fill entire row", short = false),
              Field("Field 2", "fill half of the row", short = true),
              Field("Field 3", "fill half od the row", short = true),
              Field("Field 4", "fill entire row")
            ),
            Attachment(Title("Good message"), Text("something like that")),
            Attachment(Color.warning, Field("Teraz field", "taka sytuacja"))
          )
          )
          )
