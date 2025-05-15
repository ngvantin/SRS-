"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import { fetchRoleById, Role } from "@/app/api/roles/api"
import { toast } from "@/components/ui/use-toast"
import { Toaster } from "@/components/ui/toaster"
import { Input } from "@/components/ui/input"
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from "@/components/ui/table"

export default function RoleDetails({ params }: { params: { id: string } }) {
  const { id } = params
  const [role, setRole] = useState<Role | null>(null)
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")

  useEffect(() => {
    const loadRole = async () => {
      try {
        const roleData = await fetchRoleById(Number(id))
        setRole(roleData)
      } catch (error) {
        console.error(`Failed to fetch role with ID ${id}:`, error)
        toast({
          title: "Error",
          description: "Failed to load role details",
          variant: "destructive",
        })
      } finally {
        setLoading(false)
      }
    }

    loadRole()
  }, [id])

  if (loading) {
    return <div className="container max-w-3xl mx-auto py-8 px-4 text-center">Loading role details...</div>
  }

  if (!role) {
    return <div className="container max-w-3xl mx-auto py-8 px-4 text-center">Role not found</div>
  }

  return (
    <div className="container max-w-3xl mx-auto py-8 px-4">
      <Card>
        <CardHeader>
          <h1 className="text-2xl font-bold text-center">Role Details</h1>
        </CardHeader>
        <CardContent className="p-6">
          <div className="space-y-6">
            <div className="border-b pb-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-500">Name:</p>
                  <p className="text-lg font-medium text-blue-600">{role.name}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Modules Count:</p>
                  <p className="text-lg font-medium text-blue-600">{role.modules?.length || 0}</p>
                </div>
              </div>
            </div>

            <div>
              <h2 className="text-xl font-semibold mb-4">Modules</h2>
              <div className="mb-4">
                <Input
                  placeholder="Search modules..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="max-w-xs"
                />
              </div>
              <div className="border rounded-lg overflow-hidden">
                <div className="max-h-96 overflow-y-auto">
                  <Table>
                    <TableHeader className="bg-gray-50">
                      <TableRow>
                        <TableHead>Module Name</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {role.modules && role.modules
                        .filter(module => 
                          module.name.toLowerCase().includes(searchTerm.toLowerCase())
                        )
                        .map((module) => (
                          <TableRow key={module.id} className="hover:bg-gray-50">
                            <TableCell>{module.name}</TableCell>
                          </TableRow>
                        ))}
                      {role.modules && role.modules.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={1} className="text-center py-4 text-gray-500">
                            No modules assigned
                          </TableCell>
                        </TableRow>
                      )}
                      {role.modules && role.modules.length > 0 && 
                        role.modules.filter(module => 
                          module.name.toLowerCase().includes(searchTerm.toLowerCase())
                        ).length === 0 && (
                        <TableRow>
                          <TableCell colSpan={1} className="text-center py-4 text-gray-500">
                            No modules found matching "{searchTerm}"
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </div>
            </div>

            <div className="flex justify-end mt-6">
              <Button variant="outline" asChild>
                <Link href="/roles">Back to List</Link>
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
      <Toaster />
    </div>
  )
}
