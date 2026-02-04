<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="2025.12.08" name="object" tilewidth="80" tileheight="80" tilecount="6" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="1">
  <properties>
   <property name="Heath" type="float" value="25"/>
   <property name="Speed" type="float" value="2"/>
  </properties>
  <image source="../../player/PlayerModel.png" width="16" height="16"/>
 </tile>
 <tile id="3">
  <image source="tree1.png" width="32" height="32"/>
 </tile>
 <tile id="8">
  <image source="house1.png" width="80" height="80"/>
 </tile>
 <tile id="9">
  <image source="watertoweranim0.png" width="48" height="48"/>
  <animation>
   <frame tileid="9" duration="150"/>
   <frame tileid="10" duration="150"/>
   <frame tileid="11" duration="150"/>
  </animation>
 </tile>
 <tile id="10">
  <image source="watertoweranim1.png" width="48" height="48"/>
 </tile>
 <tile id="11">
  <image source="watertoweranim2.png" width="48" height="48"/>
 </tile>
</tileset>
