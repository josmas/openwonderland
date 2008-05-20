#!/usr/bin/env ruby

# usage error
if ARGV.length != 3 then 
    $stderr.puts( "Usage: #{File.basename( $0 )} directory volname output" )
    exit( 1 )   
end                     

# get variables
dir = ARGV.shift.sub( /\/$/, "" )
volname = ARGV.shift
output = ARGV.shift

# dmg the folder
`hdiutil create -fs HFS+ -srcfolder "#{dir}" -volname "#{volname}" "#{output}"`
