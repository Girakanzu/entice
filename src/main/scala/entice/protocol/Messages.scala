/**
 * For copyright information see the LICENSE document.
 */

package entice.protocol

import play.api.libs.json._
import info.akshaal.json.jsonmacro._


/**
 * Supertype of all network messages.
 * Each network message carries its own class name in a value called "type"
 * to be able to dispatch it to a handler later on.
 */
sealed trait Message extends Typeable

// lobby/management messages:
// s->c
case class Failure              (error: String = "An unkown error occured.")    extends Message // send if requests fail, or an error occured generally


// c->s
case class LoginRequest         (email: String, 
                                password: String)                               extends Message
// s->c
case class LoginSuccess         (chars: List[EntityView])                       extends Message // convention: this will only contain CharacterViews


// c->s
case class CharCreateRequest    (name: Name,
                                appearance: Appearance)                         extends Message
case class CharDelete           (chara: Entity)                                 extends Message
// s->c
case class CharCreateSuccess    (chara: Entity)                                 extends Message


// c->s
case class PlayRequest          (chara: Entity)                                 extends Message
case class PlayReady            ()                                              extends Message // if client is ready to play after instance load
case class PlayChangeMap        (map: String)                                   extends Message { def mapData = Maps.withMapName(map) }
case class PlayQuit             ()                                              extends Message
// s->c
case class PlaySuccess          (map: String,
                                world: List[EntityView])                        extends Message { def mapData = Maps.withMapName(map) }


// ingame/gameplay messages:
case class ChatMessage          (sender: Entity,
                                message: String,                                                // bidirectional
                                channel: String)                                extends Message { def chatChannel = ChatChannels.withName(channel) }
case class ServerMessage        (message: String)                               extends Message // from server
case class ChatCommand          (command: String, 
                                args: List[String])                             extends Message // from client

// updates on the CES, from client
case class MoveRequest          (direction: Coord2D)                            extends Message // request move in this dir
case class GroupMergeRequest    (target: Entity)                                extends Message
case class GroupKickRequest     (target: Entity)                                extends Message // can be own entity to leave group

// updates on the CES, from server
case class UpdateCommand        (timeDelta: Int,
                                entityViews: List[EntityView],
                                added: List[Entity],
                                removed: List[Entity])                          extends Message


object Message {

    import Utils._
    import EntitySystem._

    // serialization
    implicit def failureFields                  = allFields[Failure]            ('jsonate)

    implicit def loginRequestFields             = allFields[LoginRequest]       ('jsonate)
    implicit def loginSuccessFields             = allFields[LoginSuccess]       ('jsonate)

    implicit def charCreateRequestFields        = allFields[CharCreateRequest]  ('jsonate)
    implicit def charCreateSuccessFields        = allFields[CharCreateSuccess]  ('jsonate)
    implicit def charDeleteFields               = allFields[CharDelete]         ('jsonate)

    implicit def playRequestFields              = allFields[PlayRequest]        ('jsonate)
    implicit def playReadyFields                = allFields[PlayReady]          ('jsonate)
    implicit def playChangeMapFields            = allFields[PlayChangeMap]      ('jsonate)
    implicit def playQuitFields                 = allFields[PlayQuit]           ('jsonate)
    implicit def playSuccessFields              = allFields[PlaySuccess]        ('jsonate)

    implicit def chatMessageFields              = allFields[ChatMessage]        ('jsonate)
    implicit def serverMessageFields            = allFields[ServerMessage]      ('jsonate)
    implicit def chatCommandFields              = allFields[ChatCommand]        ('jsonate)

    implicit def moveRequestFields              = allFields[MoveRequest]        ('jsonate)
    implicit def groupMergeRequestFields        = allFields[GroupMergeRequest]  ('jsonate)
    implicit def groupKickRequestFields         = allFields[GroupKickRequest]   ('jsonate)

    implicit def updateCommandFields            = allFields[UpdateCommand]      ('jsonate)


    implicit def messageWrites = matchingWrites[Message] {
        case c: Failure                         => failureFields                .toWrites.writes(c)

        case c: LoginRequest                    => loginRequestFields           .toWrites.writes(c)
        case c: LoginSuccess                    => loginSuccessFields           .toWrites.writes(c)

        case c: CharCreateRequest               => charCreateRequestFields      .toWrites.writes(c)
        case c: CharCreateSuccess               => charCreateSuccessFields      .toWrites.writes(c)
        case c: CharDelete                      => charDeleteFields             .toWrites.writes(c)

        case c: PlayRequest                     => playRequestFields            .toWrites.writes(c)
        case c: PlayReady                       => playReadyFields              .toWrites.writes(c)
        case c: PlayChangeMap                   => playChangeMapFields          .toWrites.writes(c)
        case c: PlayQuit                        => playQuitFields               .toWrites.writes(c)
        case c: PlaySuccess                     => playSuccessFields            .toWrites.writes(c)

        case c: ChatMessage                     => chatMessageFields            .toWrites.writes(c)
        case c: ServerMessage                   => serverMessageFields          .toWrites.writes(c)
        case c: ChatCommand                     => chatCommandFields            .toWrites.writes(c)

        case c: MoveRequest                     => moveRequestFields            .toWrites.writes(c)
        case c: GroupMergeRequest               => groupMergeRequestFields      .toWrites.writes(c)
        case c: GroupKickRequest                => groupKickRequestFields       .toWrites.writes(c)

        case c: UpdateCommand                   => updateCommandFields          .toWrites.writes(c)
    }


    // deserialization
    implicit def failureFactory                 = factory[Failure]              ('fromJson)

    implicit def loginRequestFactory            = factory[LoginRequest]         ('fromJson)
    implicit def loginSuccessFactory            = factory[LoginSuccess]         ('fromJson)

    implicit def charCreateRequestFactory       = factory[CharCreateRequest]    ('fromJson)
    implicit def charCreateSuccessFactory       = factory[CharCreateSuccess]    ('fromJson)
    implicit def charDeleteFactory              = factory[CharDelete]           ('fromJson)

    implicit def playRequestFactory             = factory[PlayRequest]          ('fromJson)
    implicit def playReadyFactory               = factory[PlayReady]            ('fromJson)
    implicit def playChangeMapFactory           = factory[PlayChangeMap]        ('fromJson)
    implicit def playQuitFactory                = factory[PlayQuit]             ('fromJson)
    implicit def playSuccessFactory             = factory[PlaySuccess]          ('fromJson)

    implicit def chatMessageFactory             = factory[ChatMessage]          ('fromJson)
    implicit def serverMessageFactory           = factory[ServerMessage]        ('fromJson)
    implicit def chatCommandFactory             = factory[ChatCommand]          ('fromJson)

    implicit def moveRequestFactory             = factory[MoveRequest]          ('fromJson)
    implicit def groupMergeRequestFactory       = factory[GroupMergeRequest]    ('fromJson)
    implicit def groupKickRequestFactory        = factory[GroupKickRequest]     ('fromJson)

    implicit def updatecommandFactory           = factory[UpdateCommand]        ('fromJson)


    implicit def messageReads: Reads[Message] =
        predicatedReads[Message](
            jsHas('type                         -> 'Failure)                    -> failureFactory,

            jsHas('type                         -> 'LoginRequest)               -> loginRequestFactory,
            jsHas('type                         -> 'LoginSuccess)               -> loginSuccessFactory,

            jsHas('type                         -> 'CharCreateRequest)          -> charCreateRequestFactory,
            jsHas('type                         -> 'CharCreateSuccess)          -> charCreateSuccessFactory,
            jsHas('type                         -> 'CharDelete)                 -> charDeleteFactory,

            jsHas('type                         -> 'PlayRequest)                -> playRequestFactory,
            jsHas('type                         -> 'PlayReady)                  -> playReadyFactory,
            jsHas('type                         -> 'PlayChangeMap)              -> playChangeMapFactory,
            jsHas('type                         -> 'PlayQuit)                   -> playQuitFactory,
            jsHas('type                         -> 'PlaySuccess)                -> playSuccessFactory,

            jsHas('type                         -> 'ChatMessage)                -> chatMessageFactory,
            jsHas('type                         -> 'ServerMessage)              -> serverMessageFactory,
            jsHas('type                         -> 'ChatCommand)                -> chatCommandFactory,
            
            jsHas('type                         -> 'MoveRequest)                -> moveRequestFactory,
            jsHas('type                         -> 'GroupMergeRequest)          -> groupMergeRequestFactory,
            jsHas('type                         -> 'GroupKickRequest)           -> groupKickRequestFactory,

            jsHas('type                         -> 'UpdateCommand)              -> updatecommandFactory
        )
}