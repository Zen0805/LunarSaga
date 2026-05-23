<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="2025.12.08" name="object" tilewidth="80" tileheight="80" tilecount="6" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="1">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="controller" type="bool" value="true"/>
   <property name="speed" type="float" value="4"/>
  </properties>
  <image source="object/player.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="12.8125" y="20.0625" width="8.1875" height="3.9375"/>
  </objectgroup>
 </tile>
 <tile id="3">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="bodyType " value="StaticBody "/>
  </properties>
  <image source="object/tree.png" width="32" height="32"/>
 </tile>
 <tile id="8">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="bodyType " value="StaticBody "/>
  </properties>
  <image source="object/house.png" width="80" height="80"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="8.36364" y="23.8182" width="63.2727" height="51.6364"/>
  </objectgroup>
 </tile>
 <tile id="9">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
  <image source="object/watertoweranim0.png" width="48" height="48"/>
  <animation>
   <frame tileid="9" duration="150"/>
   <frame tileid="10" duration="150"/>
   <frame tileid="11" duration="150"/>
  </animation>
 </tile>
 <tile id="10">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
  <image source="object/watertoweranim1.png" width="48" height="48"/>
 </tile>
 <tile id="11">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
  <image source="object/watertoweranim2.png" width="48" height="48"/>
 </tile>
</tileset>
